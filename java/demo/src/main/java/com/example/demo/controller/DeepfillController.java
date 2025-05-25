package com.example.demo.controller;

import com.example.demo.pojo.DeepfillBean;
import com.example.demo.service.DeepfillService;
import com.example.demo.util.PathUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class DeepfillController {
    private final Path segImgPath;
    private final Path oriImgPath;
    private final Path fillImgPath;

    @Autowired
    private DeepfillService deepfillService;
    private void createDirectories(Path path) {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException("无法创建目录: " + path, e);
        }
    }

    @Autowired
    public DeepfillController(
            @Value("${file.upload-dir.deepfill.segImg}") String segImgDir,
            @Value("${file.upload-dir.deepfill.oriImg}") String oriImgDir,
            @Value("${file.upload-dir.deepfill.fillImg}") String fillImgDir) {

        this.segImgPath = Paths.get(segImgDir).toAbsolutePath().normalize();
        this.oriImgPath = Paths.get(oriImgDir).toAbsolutePath().normalize();
        this.fillImgPath = Paths.get(fillImgDir).toAbsolutePath().normalize();

        // 确保目录存在
        createDirectories(this.segImgPath);
        createDirectories(this.oriImgPath);
        createDirectories(this.fillImgPath);
    }
    @PostMapping("/deepfill/upload")
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> response = new HashMap<>();

        if (file.isEmpty()) {
            log.error("file is empty");
            response.put("code", 0);
            response.put("msg", "file is empty");
            response.put("data", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        try {
            // 获取文件信息
            String fileName = file.getOriginalFilename();
            String contentType = file.getContentType();
            long size = file.getSize();

            // 验证文件类型
            if (!contentType.startsWith("image/")) {
                log.error("仅支持上传图片文件");
                response.put("code", 0);
                response.put("msg", "仅支持上传图片文件");
                response.put("data", null);
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            //分配文件名
            String saveDir = segImgPath.toString();
            String newFileName = PathUtil.getNewFileName(oriImgPath.toString());

            // 保存文件到服务器
            byte[] bytes = file.getBytes();
            Path path = Paths.get(oriImgPath + "/"+newFileName);
            Files.write(path, bytes);

            DeepfillBean deepfillBean = new DeepfillBean("/deepfill/upload/"+newFileName,null,null,null);
            log.info("deepfill upload");
            response.put("code", 1);
            response.put("msg", "图片上传成功");
            response.put("imgUrl", "/deepfill/ori_image/"+newFileName);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IOException e) {
            log.error("文件上传失败");
            response.put("code", 0);
            response.put("msg", "文件上传失败");
            response.put("data", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/deepfill/seg")
    public ResponseEntity<Map<String, Object>> segImage(@RequestParam("image") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        // 验证文件是否为空
        if (file.isEmpty()) {
            log.error("文件为空");
            response.put("code", 0);
            response.put("msg", "文件为空");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // 获取文件信息
            String fileName = file.getOriginalFilename();
            String contentType = file.getContentType();
            long size = file.getSize();

            // 验证文件类型
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                log.error("仅支持上传 JPG/PNG 格式的图片文件");
                response.put("code", 0);
                response.put("msg", "仅支持上传 JPG/PNG 格式的图片文件");
                return ResponseEntity.badRequest().body(response);
            }

            // 验证文件大小
            final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
            if (size > MAX_FILE_SIZE) {
                log.error("文件大小超过限制");
                response.put("code", 0);
                response.put("msg", "文件大小不能超过 5MB");
                return ResponseEntity.badRequest().body(response);
            }

            // 分配文件名
            String newFileName = PathUtil.getNewFileName(file, segImgPath.toString());

            // 保存文件到服务器
            byte[] bytes = file.getBytes();
            Path path = segImgPath.resolve(newFileName);
            Files.write(path, bytes);

            // 构建响应
            response.put("code", 1);
            response.put("msg", "图片上传成功");
            response.put("segImgUrl", "/deepfill/seg_image/" + newFileName);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("文件上传失败", e);
            response.put("code", 0);
            response.put("msg", "文件上传失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PostMapping("/deepfill/fill")
    public ResponseEntity<Map<String, Object>> fillImage(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String segImgUrl = request.get("segImgUrl");

        // 验证参数是否为空
        if (segImgUrl == null || segImgUrl.isEmpty()) {
            log.error("参数 segImgUrl 不能为空");
            response.put("code", 0);
            response.put("msg", "参数 segImgUrl 不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        String fileName = PathUtil.getFileName(segImgUrl);
        String oriImgPath = this.oriImgPath +"/" +fileName;
        String fillImgPath = this.fillImgPath + "/" +fileName;
        String segImgPath = this.segImgPath + "/" +fileName;

        try {
            // 构造 DeepfillBean
            DeepfillBean deepfillBean = new DeepfillBean();
            deepfillBean.setSegImgUrl(segImgPath);
            deepfillBean.setFillImgUrl(fillImgPath);
            deepfillBean.setOriImgUrl(oriImgPath);

            // 调用业务逻辑处理
            boolean success = deepfillService.runDeepfillScript(deepfillBean);

            if (success) {
                // 构建成功响应
                response.put("code", 1);
                response.put("msg", "获取成功");
                Map<String, String> data = new HashMap<>();
                data.put("resImgUrl", "seg_img/"+fileName);
                response.put("data", data);
                return ResponseEntity.ok(response);
            } else {
                // 构建失败响应
                log.error("图片修补处理失败");
                response.put("code", 0);
                response.put("msg", "图片修补处理失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            // 异常处理
            log.error("图片修补处理异常", e);
            response.put("code", 0);
            response.put("msg", "服务器内部错误");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @GetMapping("/deepfill/download")
    public ResponseEntity<?> downloadImage(@RequestParam("resImgUrl") String resImgUrl) {
        // 验证参数是否为空
        if (resImgUrl == null || resImgUrl.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("msg", "参数 resImgUrl 不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // 构造文件路径
            String fileName = PathUtil.getFileName(resImgUrl);
            Path filePath = fillImgPath.resolve(fileName);

            // 检查文件是否存在
            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 0);
                response.put("msg", "文件不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // 返回文件流
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .contentType(Files.probeContentType(filePath) != null ?
                            MediaType.parseMediaType(Files.probeContentType(filePath)) :
                            MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(Files.newInputStream(filePath)));

        } catch (IOException e) {
            // 异常处理
            log.error("文件下载失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("msg", "文件下载失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
