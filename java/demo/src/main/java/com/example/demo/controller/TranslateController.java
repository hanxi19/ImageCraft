package com.example.demo.controller;

import com.example.demo.pojo.OcrBean;
import com.example.demo.service.TranslateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/translate")
public class TranslateController {
    private final Path transTxtDir;
    private final TranslateService translateService;

    @Autowired
    public TranslateController(
            @Value("${file.upload-dir.ocr.transTxt}") String transTxtDir,
            TranslateService translateService) {
        this.transTxtDir = Paths.get(transTxtDir).toAbsolutePath().normalize();
        this.translateService = translateService;
        log.info("翻译控制器初始化完成，翻译结果目录: {}", this.transTxtDir);
    }

    /**
     * 通用文本翻译接口，不依赖OCR识别结果文件，可直接输入任意文本
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> translate(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        String text = (String) request.get("text");
        String targetLang = (String) request.get("targetLang");

        if (text == null || text.isEmpty()) {
            response.put("code", 0);
            response.put("msg", "待翻译文本不能为空");
            return ResponseEntity.badRequest().body(response);
        }
        if (targetLang == null || targetLang.isEmpty()) {
            targetLang = "en";
        }

        try {
            // 1. 将text写入临时文件
            Path tempInputFile = Files.createTempFile("translate_input_", ".txt");
            Files.writeString(tempInputFile, text, StandardCharsets.UTF_8);

            // 2. 创建业务对象
            OcrBean ocrBean = new OcrBean();
            ocrBean.setOcr_result_txt(tempInputFile.toString());
            ocrBean.setTargetLang(targetLang);

            // 3. 调用翻译服务
            boolean success = translateService.runTransScript(ocrBean);

            if (success && ocrBean.getTranslate_result_txt() != null) {
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
                // 删除临时输入文件
                Files.deleteIfExists(tempInputFile);
                return ResponseEntity.ok(response);
            } else {
                Files.deleteIfExists(tempInputFile);
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

    @GetMapping("/content")
    public ResponseEntity<Map<String, Object>> getTranslateResultContent(@RequestParam String resultFilePath) {
        Map<String, Object> response = new HashMap<>();
        try {
            Path resultPath = Paths.get(resultFilePath).toAbsolutePath().normalize();
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