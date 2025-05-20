package com.example.demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OcrBean {
    private String imageUrl;          // 原始图片路径 (OCR识别使用)
    private String lang;              // 源语言类型 (OCR识别使用)
    private String ocr_result_txt;    // OCR识别结果文件路径
    private String translate_result_txt; // 翻译结果文件路径
    private String targetLang;        // 目标翻译语言 (翻译使用)
}