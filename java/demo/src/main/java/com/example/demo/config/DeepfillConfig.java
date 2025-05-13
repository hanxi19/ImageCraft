package com.example.demo.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeepfillConfig {
    @Value("${file.upload-dir.deepfill.segImg}")
    private String deepfillSegImgLocation;

    @Value("${file.upload-dir.deepfill.oriImg}")
    private String deepfillOriImgLocation;

    @Value("${file.upload-dir.deepfill.fillImg}")
    private String deepfillFillImgLocation;

    @Value("${file.deepfill.test}")
    private String deepfillTestScriptLocation;

    @Value("${file.deepfill.weight}")
    private String deepfillWeightFileLocation;

    @Bean(name = "deepfillSegImgLocation")
    public Path deepfillSegImgLocation() {
        return createDir(deepfillSegImgLocation);
    }

    @Bean(name = "deepfillOriImgLocation")
    public Path deepfillOriImgLocation() {
        return createDir(deepfillOriImgLocation);
    }

    @Bean(name = "deepfillFillImgLocation")
    public Path deepfillFillImgLocation() {
        return createDir(deepfillFillImgLocation);
    }

    @Bean(name = "deepfillTestScriptLocation")
    public Path deepfillTestScriptLocation() {
        Path scriptPath = Paths.get(deepfillTestScriptLocation).toAbsolutePath().normalize();
        if (!Files.exists(scriptPath)) {
            throw new IllegalStateException("Python脚本不存在: " + scriptPath);
        }
        return scriptPath;
    }

    @Bean(name = "deepfillWeightFileLocation")
    public Path deepfillWeightFileLocation() {
        Path weightPath = Paths.get(deepfillWeightFileLocation).toAbsolutePath().normalize();
        if (!Files.exists(weightPath)) {
            throw new IllegalStateException("权重文件不存在: " + weightPath);
        }
        return weightPath;
    }

    private Path createDir(String path) {
        Path dir = Paths.get(path).toAbsolutePath().normalize();
        try {
            Files.createDirectories(dir);
            return dir;
        } catch (IOException e) {
            throw new IllegalStateException("无法创建目录: " + path, e);
        }
    }
}
