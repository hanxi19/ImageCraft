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

    @Value("${file.conda}")
    private String condaPath;

    @Bean(name = "pythonPath")
    public Path pythonPath() {
        Path scriptPath = Paths.get(pythonPath).toAbsolutePath().normalize();
        if (!Files.exists(scriptPath)) {
            throw new IllegalStateException("Python脚本不存在: " + scriptPath);
        }
        return scriptPath;
    }

    @Bean(name = "condaPath")
    public Path condaPath() {
        Path scriptPath = Paths.get(condaPath).toAbsolutePath().normalize();
        if (!Files.exists(scriptPath)) {
            throw new IllegalStateException("conda环境不存在: " + scriptPath);
        }
        return scriptPath;
    }
}
