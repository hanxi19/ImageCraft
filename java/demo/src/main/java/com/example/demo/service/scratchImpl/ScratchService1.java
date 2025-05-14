package com.example.demo.service.scratchImpl;

import com.example.demo.pojo.ScratchBean;
import com.example.demo.service.ScratchService;
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
public class ScratchService1 implements ScratchService {
    private final Path pythonPath;
    private final Path runScriptPath;
    private final Path root;
    @Autowired
    public ScratchService1(
            @Value("${file.python}") String pythonPath,
            @Value("${file.scratch.run}") String runScriptPath,
            @Value("${file.root-dir}") String root) {
        this.pythonPath = Paths.get(pythonPath).toAbsolutePath().normalize();
        this.runScriptPath = Paths.get(runScriptPath).toAbsolutePath().normalize();
        this.root = Paths.get(root).toAbsolutePath().normalize();
    }
    @Override
    public boolean runStyleganScript(ScratchBean scratchBean) {
        // 构建 Python 命令
        String[] command = {
            "cmd.exe", "/c", // 使用 cmd 执行命令
//            "E:\\develop\\web\\ImageCraft\\python\\Bringing-Old-Photos-Back-to-Life\\venv\\Scripts\\activate && " + // 激活虚拟环境
            root+"/python/Bringing-Old-Photos-Back-to-Life/venv/Scripts/activate &&",
            // pythonPath.toString(),
            "python",
            runScriptPath.toString(),
            "--output_folder" , scratchBean.getResImageUrl(),
            "--input_folder" , scratchBean.getOriImageUrl(),
            "--GPU", "0",
            "--with_scratch"
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
