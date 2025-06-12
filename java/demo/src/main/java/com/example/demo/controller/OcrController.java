package com.example.demo.controller;

import com.example.demo.pojo.OcrBean;
import com.example.demo.service.OcrService;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/ocr")
public class OcrController {
    private final Path oriImgDir;
    private final Path resTxtDir;
    private final OcrService ocrService;

    @Autowired
    public OcrController(
            @Value("${file.upload-dir.ocr.oriImg}") String oriImgDir,
            @Value("${file.upload-dir.ocr.resTxt}") String resTxtDir,
            OcrService ocrService) {
        this.oriImgDir = Paths.get(oriImgDir).toAbsolutePath().normalize();
        this.resTxtDir = Paths.get(resTxtDir).toAbsolutePath().normalize();
        this.ocrService = ocrService;
        log.info("OCR控制器初始化完成，图片目录: {}, 识别结果目录: {}", this.oriImgDir, this.resTxtDir);
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("image") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        if (file.isEmpty()) {
            response.put("code", 0);
            response.put("msg", "上传文件不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        final long MAX_FILE_SIZE = 50 * 1024 * 1024;
        if (file.getSize() > MAX_FILE_SIZE) {
            response.put("code", 0);
            response.put("msg", "文件大小不能超过5MB");
            return ResponseEntity.badRequest().body(response);
        }

        final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            response.put("code", 0);
            response.put("msg", "只支持JPG/PNG格式图片");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String newFilename = PathUtil.getNewFileName(file, oriImgDir.toString());
            Path targetLocation = oriImgDir.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            response.put("code", 1);
            response.put("msg", "上传成功");
            response.put("data", Map.of(
                    "imageUrl", "/ocr/images/" + newFilename,
                    "filename", newFilename
            ));

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            response.put("code", 0);
            response.put("msg", "文件上传失败");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) {
        try {
            Path imagePath = oriImgDir.resolve(filename).normalize();
            byte[] imageBytes = Files.readAllBytes(imagePath);
            String contentType = filename.toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageBytes);
        } catch (IOException e) {
            log.error("获取图片失败", e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/result/content")
    public ResponseEntity<Map<String, Object>> getOcrResultContent(@RequestParam String resultFilePath) {
        Map<String, Object> response = new HashMap<>();
        try {
            Path resultPath = Paths.get(resultFilePath).toAbsolutePath().normalize();
            if (!resultPath.startsWith(resTxtDir)) {
                response.put("code", 0);
                response.put("msg", "访问的文件路径不在允许的目录下");
                return ResponseEntity.badRequest().body(response);
            }
            if (!Files.exists(resultPath)) {
                log.error("文件不存在: {}", resultPath);
                response.put("code", 0);
                response.put("msg", "结果文件不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            String content = Files.readString(resultPath, StandardCharsets.UTF_8);
            log.info("成功读取OCR结果文件内容");

            response.put("code", 1);
            response.put("msg", "成功获取OCR结果");
            response.put("data", content);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("读取OCR结果失败", e);
            response.put("code", 0);
            response.put("msg", "读取OCR结果失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/recognize")
    public ResponseEntity<Map<String, Object>> recognizeText(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        String imageUrl = (String) request.get("imageUrl");
        if (imageUrl == null || imageUrl.isEmpty()) {
            response.put("code", 0);
            response.put("msg", "参数 imageUrl 不能为空");
            return ResponseEntity.badRequest().body(response);
        }
        String lang = (String) request.get("lang");
        if (lang == null || lang.isEmpty()) {
            lang = "ch";
        }
        try {
            String fileName = PathUtil.getFileName(imageUrl);
            String oriImagePath = oriImgDir + "/" + fileName;
            OcrBean ocrBean = new OcrBean(oriImagePath, lang, null, null, null);
            boolean success = ocrService.runOcrScript(ocrBean);
            if (success) {
                response.put("code", 1);
                response.put("msg", "识别成功");
                response.put("data", Map.of(
                        "filename", fileName,
                        "ocr_result_txt", ocrBean.getOcr_result_txt()
                ));
                log.info("OCR识别成功，结果文件路径: {}", ocrBean.getOcr_result_txt());
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 0);
                response.put("msg", "文字识别失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            log.error("文字识别处理异常", e);
            response.put("code", 0);
            response.put("msg", "服务器内部错误");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}