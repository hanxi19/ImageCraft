package com.example.demo.controller;

import com.example.demo.pojo.DeepfillBean;
import com.example.demo.util.PathUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
            String newFileName = PathUtil.getNewFileName(file,saveDir);

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
}
