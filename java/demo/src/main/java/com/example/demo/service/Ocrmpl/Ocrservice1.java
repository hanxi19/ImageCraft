package com.example.demo.service.Ocrmpl;

import com.example.demo.pojo.OcrBean;
import com.example.demo.service.OcrService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@Service
@Slf4j
public class Ocrservice1 implements OcrService {
    private final Path runScriptPath;
    private final Path resTxtDir;

    @Autowired
    public Ocrservice1(
            @Value("${file.OCR.del}") String runScriptPath,
            @Value("${file.upload-dir.ocr.resTxt}") String resTxtDir) {
        this.runScriptPath = Paths.get(runScriptPath).toAbsolutePath().normalize();
        this.resTxtDir = Paths.get(resTxtDir).toAbsolutePath().normalize();
        log.info("OCR服务初始化完成，脚本路径: {}, 结果目录: {}", this.runScriptPath, this.resTxtDir);
    }

    @Override
    public boolean runOcrScript(OcrBean ocrBean) {
        String[] command = {
                "cmd.exe", "/c",
                "conda", "activate", "D:\\miniconda\\envs\\pdf", "&&",
                "python",
                runScriptPath.toString(),
                "--image", ocrBean.getImageUrl(),
                "--lang", ocrBean.getLang(),
                "--output_path", resTxtDir.toString()
        };

        log.info("执行OCR命令: {}", Arrays.toString(command));

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            processBuilder.directory(runScriptPath.getParent().toFile());

            Process process = processBuilder.start();

            // 读取输出
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("OCR输出: {}", line);
                    if (line.contains("result saved to:")) {
                        // 提取文件路径
                        String resultFilePath = line.substring(line.indexOf(":") + 1).trim();
                        ocrBean.setOcr_result_txt(resultFilePath); // 设置结果文件路径
                    }
                }
            }

            int exitCode = process.waitFor();
            return exitCode == 0;

        } catch (IOException | InterruptedException e) {
            log.error("执行OCR脚本时发生异常", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }
}