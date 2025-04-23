package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StyleganConfig {
    @Value("${file.stylegan.generate}")
    private String styleganGenerateLocation;

    @Value("${file.stylegan.weight}")
    private String styleganWeightLocation;


    @Bean(name = "styleganGenerateScriptPath")
    public Path styleganGenerateScriptPath() {
        Path scriptPath = Paths.get(styleganGenerateLocation).toAbsolutePath().normalize();
        if (!Files.exists(scriptPath)) {
            throw new IllegalStateException("Python脚本不存在: " + scriptPath);
        }
        return scriptPath;
    }
    @Bean(name = "styleganWeightLocation")
    public Path styleganWeightImgPath() {
        return createDir(styleganWeightLocation);
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
