package com.example.demo.service.DeepfillImpl;

import com.example.demo.pojo.DeepfillBean;
import com.example.demo.service.DeepfillService;
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
@Slf4j
@Service
public class DeepfillService1 implements DeepfillService {
    private final Path testScriptPath;
    private final Path weightPath;
    private final Path oriImgDir;
    private final Path fillImgDir;
    private final Path segImgDir;
    @Autowired
    public DeepfillService1(
            @Value("${file.deepfill.test}") String testScriptPath,
            @Value("${file.deepfill.weight}") String weightPath,
            @Value("${file.upload-dir.deepfill.oriImg}") String oriImgDir,
            @Value("${file.upload-dir.deepfill.segImg}") String segImgDir,
            @Value("${file.upload-dir.deepfill.fillImg}") String fillImgDir) {
        this.testScriptPath = Paths.get(testScriptPath).toAbsolutePath().normalize();
        this.oriImgDir = Paths.get(oriImgDir).toAbsolutePath().normalize();
        this.segImgDir = Paths.get(segImgDir).toAbsolutePath().normalize();
        this.fillImgDir = Paths.get(fillImgDir).toAbsolutePath().normalize();
        this.weightPath = Paths.get(weightPath).toAbsolutePath().normalize();
    }
    @Override
    public boolean runDeepfillScript(DeepfillBean deepfillBean) {
        // 构建 Python 命令
        String[] command = {
                "cmd.exe", "/c", // 使用 cmd 执行命令
//            "E:\\develop\\web\\ImageCraft\\python\\Bringing-Old-Photos-Back-to-Life\\venv\\Scripts\\activate && " + // 激活虚拟环境
                "conda activate E:\\develop\\web\\ImageCraft\\python\\imgc && ",
                // pythonPath.toString(),
                "python",
                testScriptPath.toString(),
                "--image" , deepfillBean.getOriImgUrl(),
                "--mask" , deepfillBean.getSegImgUrl(),
                "--out" , deepfillBean.getFillImgUrl(),
                "--checkpoint" , weightPath.toString(),
        };
        log.info("command:"+ Arrays.toString(command));

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true); // 合并标准错误和标准输出
            processBuilder.directory(testScriptPath.getParent().toFile()); // 设置工作目录为脚本所在目录


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
