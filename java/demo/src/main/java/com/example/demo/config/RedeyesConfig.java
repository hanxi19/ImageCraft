package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class RedeyesConfig {
    @Value("${file.upload-dir.redeyes.oriImg}")
    private String redeyesConfigOriImgLocation;

    @Value("${file.upload-dir.redeyes.resImg}")
    private String redeyesConfigResImgLocation;

    @Value("${file.redeyes.del}")
    private String redeyesConfigDelLocation;

    @Bean(name = "redeyesConfigDelLocation")
    public Path redeyesConfigDelLocation() {
        Path scriptPath = Paths.get(redeyesConfigDelLocation).toAbsolutePath().normalize();
        if (!Files.exists(scriptPath)) {
            throw new IllegalStateException("Python脚本不存在: " + scriptPath);
        }
        return scriptPath;
    }

    @Bean(name = "redeyesConfigOriImgLocation")
    public Path redeyesConfigOriImgLocation() {
        return createDir(redeyesConfigOriImgLocation);
    }

    @Bean(name = "redeyesConfigResImgLocation")
    public Path redeyesConfigResImgLocation() {
        return createDir(redeyesConfigResImgLocation);
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
