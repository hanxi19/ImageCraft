package com.example.demo.controller;

import com.example.demo.pojo.SegmentBean;
import com.example.demo.service.SegmentService;
import com.example.demo.util.PathUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Slf4j
@RestController
@RequestMapping
public class SegmentController {
    private final Path oriImgDir;
    private final Path resImgDir;

    @Autowired
    private SegmentService segmentService;

    @Autowired
    public SegmentController(
            @Value("${file.upload-dir.segment.oriImg}") String oriImgDir,
            @Value("${file.upload-dir.segment.resImg}") String resImgDir) {
        this.oriImgDir = Paths.get(oriImgDir).toAbsolutePath().normalize();
        this.resImgDir = Paths.get(resImgDir).toAbsolutePath().normalize();
    }

    @PostMapping("/segment/upload")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("image") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        // 验证文件是否为空
        if (file.isEmpty()) {
            response.put("code", 0);
            response.put("msg", "上传文件不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        // 验证文件大小 (5MB限制)
        final long MAX_FILE_SIZE = 50 * 1024 * 1024;
        if (file.getSize() > MAX_FILE_SIZE) {
            response.put("code", 0);
            response.put("msg", "文件大小不能超过5MB");
            return ResponseEntity.badRequest().body(response);
        }

        // 验证文件类型
        final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            response.put("code", 0);
            response.put("msg", "只支持JPG/PNG格式图片");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // 生成唯一文件名
            String newFilename = PathUtil.getNewFileName(file, oriImgDir.toString());

            // 保存文件
            Path targetLocation = oriImgDir.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 构建响应
            response.put("code", 1);
            response.put("msg", "上传成功");
            response.put("data", Map.of(
                    "imageUrl", "/segment/images/" + newFilename,  // 返回的访问URL
                    "previewWidth", 500,  // 建议预览宽度
                    "previewHeight", 500  // 建议预览高度
            ));

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            response.put("code", 0);
            response.put("msg", "文件上传失败");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/segment/images/{filename:.+}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) {
        try {
            Path imagePath = oriImgDir.resolve(filename).normalize();
            byte[] imageBytes = Files.readAllBytes(imagePath);

            // 简单判断图片类型
            String contentType = filename.toLowerCase().endsWith(".png")
                    ? "image/png"
                    : "image/jpeg";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageBytes);
        } catch (IOException e) {
            log.error("获取图片失败", e);
            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping("/segment/process")
    public ResponseEntity<Map<String, Object>> processImage(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        // 提取 imageURL 参数
        String imageUrl = (String) request.get("imageUrl");
        if (imageUrl == null || imageUrl.isEmpty()) {
            response.put("code", 0);
            response.put("msg", "参数 imageUrl 不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        // 提取 keyPoints 参数
        Object pointsObj = request.get("keyPoints");
        if (!(pointsObj instanceof List)) {
            response.put("code", 0);
            response.put("msg", "参数 keyPoints 格式错误");
            return ResponseEntity.badRequest().body(response);
        }

        List<Double> keyPoints = new ArrayList<>();
        try {
            // 转换坐标点
            for (Object point : (List<?>) pointsObj) {
                if (point instanceof Number) {
                    keyPoints.add(((Number) point).doubleValue());
                } else if (point instanceof Map) {
                    Map<?, ?> map = (Map<?, ?>) point;
                    Object xObj = map.get("x");
                    Object yObj = map.get("y");
                    if (xObj instanceof Number && yObj instanceof Number) {
                        keyPoints.add(((Number) xObj).doubleValue());
                        keyPoints.add(((Number) yObj).doubleValue());
                    } else {
                        throw new IllegalArgumentException("坐标点x/y必须是数字类型");
                    }
                } else {
                    throw new IllegalArgumentException("坐标点必须是数字类型或包含x/y的对象");
                }
            }

            // 检查坐标点数量是否为偶数
            if (keyPoints.size() % 2 != 0) {
                throw new IllegalArgumentException("坐标点数量必须为偶数");
            }
        } catch (Exception e) {
            response.put("code", 0);
            response.put("msg", "参数 keyPoints 格式错误：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // 构造文件路径
            String fileName = PathUtil.getFileName(imageUrl);
            String oriImagePath = oriImgDir + "/" + fileName;
            String resImagePath = resImgDir + "/" + fileName;

            // 创建业务对象
            SegmentBean segmentBean = new SegmentBean(oriImagePath, keyPoints, resImagePath);

            // 调用业务处理
            boolean success = segmentService.runSegmentScript(segmentBean);
            String resultUrl = "/segment/result/" + fileName;

            if (success) {
                response.put("code", 1);
                response.put("msg", "分割成功");
                response.put("data", Map.of(
                        "segmentationResultUrl", resultUrl
                ));
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 0);
                response.put("msg", "图片分割失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            log.error("图片分割处理异常", e);
            response.put("code", 0);
            response.put("msg", "服务器内部错误");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/segment/result/{filename:.+}")
    public ResponseEntity<byte[]> getResultImage(@PathVariable String filename) {
        try {
            Path imagePath = resImgDir.resolve(filename).normalize();
            byte[] imageBytes = Files.readAllBytes(imagePath);

            // 简单判断图片类型
            String contentType = filename.toLowerCase().endsWith(".png")
                    ? "image/png"
                    : "image/jpeg";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageBytes);
        } catch (IOException e) {
            log.error("获取结果图片失败", e);
            return ResponseEntity.notFound().build();
        }
    }
}
