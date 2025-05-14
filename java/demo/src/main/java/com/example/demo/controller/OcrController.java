package com.example.demo.controller;

import com.example.demo.pojo.OcrBean;
import com.example.demo.service.OcrService;
import com.example.demo.service.TranslateService;
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
    private final Path transTxtDir;
    private final OcrService ocrService;
    private final TranslateService translateService;

    @Autowired
    public OcrController(
            @Value("${file.upload-dir.ocr.oriImg}") String oriImgDir,
            @Value("${file.upload-dir.ocr.resTxt}") String resTxtDir,
            @Value("${file.upload-dir.ocr.transTxt}") String transTxtDir,
            OcrService ocrService,
            TranslateService translateService) {
        this.oriImgDir = Paths.get(oriImgDir).toAbsolutePath().normalize();
        this.resTxtDir = Paths.get(resTxtDir).toAbsolutePath().normalize();
        this.transTxtDir = Paths.get(transTxtDir).toAbsolutePath().normalize();
        this.ocrService = ocrService;
        this.translateService = translateService;
        log.info("OCR控制器初始化完成，图片目录: {}, 识别结果目录: {}, 翻译结果目录: {}",
                this.oriImgDir, this.resTxtDir, this.transTxtDir);
    }

    @PostMapping("/upload")
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

    @GetMapping("/result/content")
    public ResponseEntity<Map<String, Object>> getOcrResultContent(@RequestParam String resultFilePath) {
        Map<String, Object> response = new HashMap<>();

        try {
            Path resultPath = Paths.get(resultFilePath).toAbsolutePath().normalize();

            // 安全检查：确保路径在允许的目录下
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

        // 提取 imageURL 参数
        String imageUrl = (String) request.get("imageUrl");
        if (imageUrl == null || imageUrl.isEmpty()) {
            response.put("code", 0);
            response.put("msg", "参数 imageUrl 不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        // 提取 lang 参数
        String lang = (String) request.get("lang");
        if (lang == null || lang.isEmpty()) {
            lang = "ch"; // 默认中文
        }

        try {
            // 构造文件路径
            String fileName = PathUtil.getFileName(imageUrl);
            String oriImagePath = oriImgDir + "/" + fileName;

            // 创建业务对象
            OcrBean ocrBean = new OcrBean(oriImagePath, lang, null, null, null);

            // 调用OCR识别服务
            boolean success = ocrService.runOcrScript(ocrBean);

            if (success) {
                response.put("code", 1);
                response.put("msg", "识别成功");
                response.put("data", Map.of(
                        "filename", fileName,
                        "ocr_result_txt", ocrBean.getOcr_result_txt() // 返回结果文件路径
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

    @PostMapping("/translate")
    public ResponseEntity<Map<String, Object>> translateText(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        // 提取参数
        String ocrResultPath = (String) request.get("ocrResultPath");
        String targetLang = (String) request.get("targetLang");
        String editedText = (String) request.get("editedText");

        if (ocrResultPath == null || ocrResultPath.isEmpty()) {
            response.put("code", 0);
            response.put("msg", "参数 ocrResultPath 不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        if (targetLang == null || targetLang.isEmpty()) {
            targetLang = "en"; // 默认英文
        }

        try {
            // 1. 保存编辑后的文本到原OCR结果文件
            Path resultPath = Paths.get(ocrResultPath).toAbsolutePath().normalize();

            // 安全检查：确保路径在允许的目录下
            if (!resultPath.startsWith(resTxtDir)) {
                response.put("code", 0);
                response.put("msg", "访问的文件路径不在允许的目录下");
                return ResponseEntity.badRequest().body(response);
            }

            Files.writeString(resultPath, editedText, StandardCharsets.UTF_8);

            // 2. 创建业务对象并设置翻译语言
            OcrBean ocrBean = new OcrBean();
            ocrBean.setOcr_result_txt(ocrResultPath);
            ocrBean.setTargetLang(targetLang);

            // 3. 调用翻译服务
            boolean success = translateService.runTransScript(ocrBean);

            if (success && ocrBean.getTranslate_result_txt() != null) {
                // 4. 读取翻译结果
                String translatedContent = Files.readString(
                        Paths.get(ocrBean.getTranslate_result_txt()),
                        StandardCharsets.UTF_8
                );

                response.put("code", 1);
                response.put("msg", "翻译成功");
                response.put("data", Map.of(
                        "translatedText", translatedContent,
                        "translateResultPath", ocrBean.getTranslate_result_txt()
                ));
                log.info("翻译成功，结果文件路径: {}", ocrBean.getTranslate_result_txt());
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 0);
                response.put("msg", "翻译失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            log.error("翻译处理异常", e);
            response.put("code", 0);
            response.put("msg", "服务器内部错误");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/translate/content")
    public ResponseEntity<Map<String, Object>> getTranslateResultContent(@RequestParam String resultFilePath) {
        Map<String, Object> response = new HashMap<>();

        try {
            Path resultPath = Paths.get(resultFilePath).toAbsolutePath().normalize();

            // 安全检查：确保路径在允许的目录下
            if (!resultPath.startsWith(transTxtDir)) {
                response.put("code", 0);
                response.put("msg", "访问的文件路径不在允许的目录下");
                return ResponseEntity.badRequest().body(response);
            }

            if (!Files.exists(resultPath)) {
                log.error("文件不存在: {}", resultPath);
                response.put("code", 0);
                response.put("msg", "翻译结果文件不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            String content = Files.readString(resultPath, StandardCharsets.UTF_8);
            log.info("成功读取翻译结果文件内容");

            response.put("code", 1);
            response.put("msg", "成功获取翻译结果");
            response.put("data", content);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("读取翻译结果失败", e);
            response.put("code", 0);
            response.put("msg", "读取翻译结果失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}