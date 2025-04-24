package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class ScratchConfig {
    @Value("${file.scratch.run}")
    private String scratchConfigRunLocation;

    @Value("${file.scratch.python}")
    private String scratchPythonRunLocation;
    @Bean(name = "scratchConfigRunLocation")
    public Path scratchConfigRunLocation() {
        Path scriptPath = Paths.get(scratchConfigRunLocation).toAbsolutePath().normalize();
        if (!Files.exists(scriptPath)) {
            throw new IllegalStateException("Python脚本不存在: " + scriptPath);
        }
        return scriptPath;
    }
    @Bean(name = "scratchPythonRunLocation")
    public Path scratchPythonRunLocation() {
        Path scriptPath = Paths.get(scratchPythonRunLocation).toAbsolutePath().normalize();
        if (!Files.exists(scriptPath)) {
            throw new IllegalStateException("Python脚本不存在: " + scriptPath);
        }
        return scriptPath;
    }
}
