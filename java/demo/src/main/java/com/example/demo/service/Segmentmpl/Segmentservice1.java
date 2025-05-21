package com.example.demo.service.Segmentmpl;

import com.example.demo.pojo.SegmentBean;
import com.example.demo.service.SegmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class Segmentservice1 implements SegmentService {
    private final Path runScriptPath;
    private final Path root;
    private final Path oriImgDir;
    private final Path resImgDir;
    private final String modelType = "vit_b"; // 固定模型类型
    private final Path checkpointPath; // 固定权重文件路径
    private final Path condaPath;

    @Autowired
    public Segmentservice1(
            @Value("${file.segment.del}") String runScriptPath,
            @Value("${file.root-dir}") String root,
            @Value("${file.upload-dir.segment.oriImg}") String oriImgDir,
            @Value("${file.upload-dir.segment.resImg}") String resImgDir,
            @Value("${file.segment.checkpoint}") String checkpointPath,
            @Qualifier("segment_ocr_translate_conda") Path condaPath) {
        this.runScriptPath = Paths.get(runScriptPath).toAbsolutePath().normalize();
        this.root = Paths.get(root).toAbsolutePath().normalize();
        this.oriImgDir = Paths.get(oriImgDir).toAbsolutePath().normalize();
        this.resImgDir = Paths.get(resImgDir).toAbsolutePath().normalize();
        this.checkpointPath = Paths.get(checkpointPath).toAbsolutePath().normalize();
        this.condaPath = condaPath;

        log.info("Segment服务初始化完成，脚本路径: {}", this.runScriptPath);
    }

    @Override
    public boolean runSegmentScript(SegmentBean segmentBean) {
        // 获取坐标点字符串
        String points = segmentBean.convertPointsToString();

        // 构建 Python 命令
        String[] command = {
                "cmd.exe", "/c", // 使用 cmd 执行命令
                "conda activate " + condaPath.toString() + " && ", // 激活conda环境
                "python",
                runScriptPath.toString(),
                "--image", segmentBean.getImageUrl(),
                "--points", points,
                "--output", segmentBean.getSegmentationResultUrl(),
                "--model_type", modelType,
                "--checkpoint", checkpointPath.toString()
        };

        log.info("执行Segment命令: {}", Arrays.toString(command));

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true); // 合并标准错误和标准输出
            processBuilder.directory(runScriptPath.getParent().toFile()); // 设置工作目录为脚本所在目录

            Process process = processBuilder.start();

            // 读取输出（用于调试和日志记录）
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("[Python输出] {}", line); // 使用日志记录Python输出
            }

            int exitCode = process.waitFor();
            boolean success = exitCode == 0;

            if (success) {
                log.info("Segment处理成功，输出文件: {}", segmentBean.getSegmentationResultUrl());
            } else {
                log.error("Segment处理失败，退出码: {}", exitCode);
            }

            return success;

        } catch (IOException | InterruptedException e) {
            log.error("执行Segment脚本时发生异常", e);
            return false;
        }
    }
}