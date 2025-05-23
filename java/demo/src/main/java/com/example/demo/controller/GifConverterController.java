package com.example.demo.controller;

import com.madgag.gif.fmsware.AnimatedGifEncoder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
class GifConverterController {

    @PostMapping(value = "/api/convert", produces = MediaType.IMAGE_GIF_VALUE)
    public ResponseEntity<?> convertToGif(
            @RequestParam("files") List<MultipartFile> files) throws IOException {

        // 检查文件连续性
        List<Integer> sequenceNumbers = new ArrayList<>();
        for (MultipartFile file : files) {
            String filename = file.getOriginalFilename();
            Matcher matcher = Pattern.compile("\\d+").matcher(filename);
            if (!matcher.find()) {
                return ResponseEntity.badRequest().body("文件名缺少序列号: " + filename);
            }
            sequenceNumbers.add(Integer.parseInt(matcher.group()));
        }

        // 验证连续性
        Collections.sort(sequenceNumbers);
        for (int i = 1; i < sequenceNumbers.size(); i++) {
            if (sequenceNumbers.get(i) - sequenceNumbers.get(i-1) != 1) {
                return ResponseEntity.badRequest().body("图片序列不连续");
            }
        }

        // 生成GIF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        encoder.start(outputStream);
        encoder.setDelay(200);
        encoder.setRepeat(0);

        for (MultipartFile file : files) {
            BufferedImage image = ImageIO.read(file.getInputStream());
            encoder.addFrame(image);
        }
        encoder.finish();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=animation.gif")
                .contentType(MediaType.IMAGE_GIF)
                .body(outputStream.toByteArray());
    }
}