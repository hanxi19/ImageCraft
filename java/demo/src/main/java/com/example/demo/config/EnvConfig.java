package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class EnvConfig {
    @Value("${file.python}")
    private String pythonPath;

    @Bean(name = "pythonPath")
    public Path pythonPath() {
        Path scriptPath = Paths.get(pythonPath).toAbsolutePath().normalize();
        if (!Files.exists(scriptPath)) {
            throw new IllegalStateException("Python脚本不存在: " + scriptPath);
        }
        return scriptPath;
    }
}
