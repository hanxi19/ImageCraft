package com.example.demo.service.styleganimpl;

import com.example.demo.pojo.StyleganBean;
import com.example.demo.service.StyleganService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class StyleganService1 implements StyleganService {
    private final Path generatePath;
    private final Path weightsPath;
    private final Path outputDir;
    private final Path pythonPath;
    @Autowired
    public StyleganService1(
            @Value("${file.stylegan.generate}") String generatePath,
            @Value("${file.stylegan.weight}") String weightsPath,
            @Value("${file.upload-dir.stylegan.resImg}") String outputDir,
            @Value("${file.python}") String pythonPath) {
        this.generatePath = Paths.get(generatePath).toAbsolutePath().normalize();
        this.weightsPath = Paths.get(weightsPath).toAbsolutePath().normalize();
        this.outputDir = Paths.get(outputDir).toAbsolutePath().normalize();
        this.pythonPath = Paths.get(pythonPath).toAbsolutePath().normalize();
    }

    @Override
    public boolean runStyleganScript(StyleganBean styleganBean) {
        // 1. 参数校验
        if (styleganBean == null) {
            throw new IllegalArgumentException("StyleganBean不能为null");
        }

        // 2. 准备参数映射
        Map<Integer, String> networkMap = new HashMap<>();
        networkMap.put(0, "afhqcat.pkl");  // 猫
        networkMap.put(1, "afhqdog.pkl");  // 狗
        networkMap.put(2, "metfaces.pkl"); // 艺术人像

        // 3. 构建命令参数
        String pythonScript = generatePath.toString();
        String outdir = outputDir.toString(); // 假设resImgUrl是输出目录
        String seeds = String.valueOf(styleganBean.getSeed());
        String trunc = String.valueOf(styleganBean.getTrunc());
        String networkFile = networkMap.get(styleganBean.getNetwork());
        String fileName = styleganBean.getFileName();

        if (networkFile == null) {
            throw new IllegalArgumentException("无效的network参数: " + styleganBean.getNetwork());
        }

        // 4. 构建完整命令
        String[] command = {
                pythonPath.toString(),
                pythonScript,
                "--outdir=" + outdir,
                "--seeds=" + seeds,
                "--trunc=" + trunc,
                "--network=" + weightsPath+"/"+networkFile,
                "--output-name=" + fileName
        };
        log.info("command:"+ Arrays.toString(command));

        // 5. 执行命令
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true); // 合并标准错误和标准输出

            Process process = processBuilder.start();

            // 读取输出（用于调试）
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            return exitCode == 0;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
}
