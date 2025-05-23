package com.example.demo.service.redeyesImpl;

import com.example.demo.pojo.RedeyesBean;
import com.example.demo.service.RedeyesService;
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

@Service
@Slf4j
public class RedeyesService1 implements RedeyesService {
    private final Path runScriptPath;
    private final Path root;
    private final Path oriImgDir;
    private final Path resImgDir;
    private final Path condaPath;
    @Autowired
    public RedeyesService1(
            @Value("${file.redeyes.del}") String runScriptPath,
            @Value("${file.root-dir}") String root,
            @Value("${file.upload-dir.redeyes.oriImg}") String oriImgDir,
            @Value("${file.upload-dir.redeyes.resImg}") String resImgDir,
            @Value("${file.conda}") String condaPath) {
        this.runScriptPath = Paths.get(runScriptPath).toAbsolutePath().normalize();
        this.root = Paths.get(root).toAbsolutePath().normalize();
        this.oriImgDir = Paths.get(oriImgDir).toAbsolutePath().normalize();
        this.resImgDir = Paths.get(resImgDir).toAbsolutePath().normalize();
        this.condaPath = Paths.get(condaPath).toAbsolutePath().normalize();
    }
    @Override
    public boolean runRedeyesScript(RedeyesBean redeyesBean) {
        String coords=redeyesBean.convertEyesToString();
        // 构建 Python 命令
        String[] command = {
                "cmd.exe", "/c", // 使用 cmd 执行命令
//            "E:\\develop\\web\\ImageCraft\\python\\Bringing-Old-Photos-Back-to-Life\\venv\\Scripts\\activate && " + // 激活虚拟环境
                "conda activate " + condaPath+" && ",
                // pythonPath.toString(),
                "python",
                runScriptPath.toString(),
                "--output" , redeyesBean.getResImageUrl(),
                "--input" , redeyesBean.getOriImageUrl(),
                "--coords" , coords
        };
        log.info("command:"+ Arrays.toString(command));

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true); // 合并标准错误和标准输出
            processBuilder.directory(runScriptPath.getParent().toFile()); // 设置工作目录为脚本所在目录


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
