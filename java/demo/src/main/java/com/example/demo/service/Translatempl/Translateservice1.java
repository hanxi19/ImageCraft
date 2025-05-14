package com.example.demo.service.Translatempl;

import com.example.demo.pojo.OcrBean;
import com.example.demo.service.TranslateService;
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
import java.util.Map;

@Service
@Slf4j
public class Translateservice1 implements TranslateService {
    private final Path runScriptPath;
    private final Path transTxtDir;
    private final String secretId;
    private final String secretKey;

    @Autowired
    public Translateservice1(
            @Value("${file.Translate.del}") String runScriptPath,
            @Value("${file.upload-dir.ocr.transTxt}") String transTxtDir,
            @Value("${tencent.cloud.secretId}") String secretId,
            @Value("${tencent.cloud.secretKey}") String secretKey) {
        this.runScriptPath = Paths.get(runScriptPath).toAbsolutePath().normalize();
        this.transTxtDir = Paths.get(transTxtDir).toAbsolutePath().normalize();
        this.secretId = secretId;
        this.secretKey = secretKey;
        log.info("翻译服务初始化完成，脚本路径: {}, 结果目录: {}", this.runScriptPath, this.transTxtDir);
    }

    @Override
    public boolean runTransScript(OcrBean ocrBean) {
        // 检查是否有OCR结果文件
        if (ocrBean.getOcr_result_txt() == null || ocrBean.getOcr_result_txt().isEmpty()) {
            log.error("没有OCR结果文件可供翻译");
            return false;
        }

        String[] command = {
                "cmd.exe", "/c",
                "conda", "activate", "D:\\miniconda\\envs\\pdf", "&&",
                "python",
                runScriptPath.toString(),
                "--input_file", ocrBean.getOcr_result_txt(),
                "--target", ocrBean.getTargetLang() != null ? ocrBean.getTargetLang() : "en",
                "--output_path", transTxtDir.toString()
        };

        log.info("执行翻译命令: {}", Arrays.toString(command));

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            processBuilder.directory(runScriptPath.getParent().toFile());

            // 设置环境变量
            Map<String, String> env = processBuilder.environment();
            env.put("PYTHONIOENCODING", "utf-8");
            env.put("PYTHONUTF8", "1");
            env.put("TENCENT_SECRET_ID", secretId);
            env.put("TENCENT_SECRET_KEY", secretKey);

            Process process = processBuilder.start();

            // 读取输出
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("翻译输出: {}", line);
                    if (line.contains("result saved to:")) {
                        // 提取翻译结果文件路径
                        String resultFilePath = line.substring(line.indexOf(":") + 1).trim();
                        ocrBean.setTranslate_result_txt(resultFilePath);
                    }
                }
            }

            int exitCode = process.waitFor();
            return exitCode == 0 && ocrBean.getTranslate_result_txt() != null;

        } catch (IOException | InterruptedException e) {
            log.error("执行翻译脚本时发生异常", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }
}