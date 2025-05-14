package com.example.demo.controller;

import com.example.demo.pojo.RedeyesBean;
import com.example.demo.service.RedeyesService;
import com.example.demo.util.PathUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class RedeyesController {
    private final Path oriImgDir;

    private final Path resImgDir;

    @Autowired
    private RedeyesService redeyesService;

    @Autowired
    public RedeyesController(
            @Value("${file.upload-dir.redeyes.resImg}") String oriImgDir,
            @Value("${file.upload-dir.redeyes.resImg}") String resImgDir) {

        this.oriImgDir = Paths.get(oriImgDir).toAbsolutePath().normalize();
        this.resImgDir = Paths.get(resImgDir).toAbsolutePath().normalize();
    }

    @PostMapping("/redeyes/uploads")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("image") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        // 验证文件是否为空
        if (file.isEmpty()) {
            response.put("code", 0);
            response.put("msg", "上传文件不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        // 验证文件大小
        final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
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

        // 生成唯一文件名
        String newFilename = PathUtil.getNewFileName(file,oriImgDir.toString());

        try {
            // 提取文件名序号
            String fileSequence = newFilename;

            // 保存文件到 oriImgDir
            Path targetLocation = oriImgDir.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 构建响应
            response.put("code", 1);
            response.put("msg", "上传成功");
            response.put("oriImgUrl", "/redeyes/origin_image/" + newFilename);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            response.put("code", 0);
            response.put("msg", "文件上传失败");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    @PostMapping("/redeyes/dispose")
    public ResponseEntity<Map<String, Object>> disposeImage(@RequestBody Map<String, Object> request){
        Map<String, Object> response = new HashMap<>();
//        String oriImgUrl = request.get("oriImgUrl");

//        Object eyesObj = request.get("eyes");
//        if (!(eyesObj instanceof List)) {
//            response.put("code", 0);
//            response.put("msg", "参数 eyes 格式错误");
//            return ResponseEntity.badRequest().body(response);
//        }
        // 提取 imageURL 参数
        String oriImgUrl = (String) request.get("oriImgUrl");
        if (oriImgUrl == null || oriImgUrl.isEmpty()) {
            response.put("code", 0);
            response.put("msg", "参数 imageURL 不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        // 提取 eyes 参数
        Object eyesObj = request.get("eyes");
        if (!(eyesObj instanceof List)) {
            response.put("code", 0);
            response.put("msg", "参数 eyes 格式错误");
            return ResponseEntity.badRequest().body(response);
        }

        List<List<Double>> eyes = new ArrayList<>();
        try {
            List<?> eyesList = (List<?>) eyesObj;
            for (Object rectObj : eyesList) {
                if (!(rectObj instanceof List)) {
                    throw new IllegalArgumentException("eyes 中的每个矩形框必须是列表");
                }
                List<?> rect = (List<?>) rectObj;
                if (rect.size() != 4) {
                    throw new IllegalArgumentException("每个矩形框必须包含4个元素 (x, y, w, h)");
                }
                List<Double> rectDouble = rect.stream()
                        .map(Object::toString)
                        .map(Double::valueOf)
                        .collect(Collectors.toList());
                eyes.add(rectDouble);
            }
        } catch (Exception e) {
            response.put("code", 0);
            response.put("msg", "参数 eyes 格式错误：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        try {
            // 获取请求参数
            if (oriImgUrl == null || oriImgUrl.isEmpty()) {
                response.put("code", 0);
                response.put("msg", "参数 oriImgUrl 不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 构造输出图片路径
            String fileName = PathUtil.getFileName(oriImgUrl);
            oriImgUrl = oriImgDir+"/"+fileName;
            String resImgUrl = resImgDir+"/"+fileName;

            // 构造 ScratchBean
            RedeyesBean redeyesBean = new RedeyesBean(oriImgUrl, resImgUrl,eyes);

            // 调用业务逻辑
            boolean success = redeyesService.runRedeyesScript(redeyesBean);
            resImgUrl = "/redeyes/dispose/"+fileName;

            if (success) {
                // 成功响应
                response.put("code", 1);
                response.put("msg", "获取成功");
                Map<String, String> data = new HashMap<>();
                data.put("resImgUrl", resImgUrl);
                response.put("data", data);
                return ResponseEntity.ok(response);
            } else {
                // 失败响应
                response.put("code", 0);
                response.put("msg", "图片去划痕失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            // 异常处理
            log.error("图片去划痕处理异常", e);
            response.put("code", 0);
            response.put("msg", "服务器内部错误");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PostMapping("/redeyes/download")
    public ResponseEntity<?> downloadImage(@RequestBody Map<String, String> request) {
        String resImgUrl = request.get("resImgUrl");
        try {
            // 检查参数是否为空
            if (resImgUrl == null || resImgUrl.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 0,
                        "msg", "参数 resImgUrl 不能为空"
                ));
            }

            // 构造图片文件路径
            String fileName = PathUtil.getFileName(resImgUrl);
            String filePath = resImgDir.toString() + "/" + fileName ;
            Path imagePath = Paths.get(filePath).toAbsolutePath().normalize();
            File imageFile = imagePath.toFile();

            // 检查文件是否存在
            if (!imageFile.exists() || !imageFile.isFile()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "code", 0,
                        "msg", "图片文件不存在"
                ));
            }

            // 返回图片文件流
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + imageFile.getName() + "\"")
                    .contentType(Files.probeContentType(imagePath) != null ?
                            MediaType.parseMediaType(Files.probeContentType(imagePath)) :
                            MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(new FileInputStream(imageFile)));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "code", 0,
                    "msg", "服务器内部错误"
            ));
        }
    }
}
