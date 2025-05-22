package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileStorageConfig {
    @Value("${file.root-dir}")
    private String rootDir;

    @Value("$ile.segment_ocr_translate.conda}")
    private String segment_ocr_translate_conda;

    // Deepfill 相关路径
    @Value("${file.upload-dir.deepfill.segImg}")
    private String deepfillSegImgLocation;

    @Value("${file.upload-dir.deepfill.oriImg}")
    private String deepfillOriImgLocation;

    @Value("${file.upload-dir.deepfill.fillImg}")
    private String deepfillFillImgLocation;

    // Denoising 相关路径
    @Value("${file.upload-dir.denoising.resImg}")
    private String denoisingResImgLocation;

    @Value("${file.upload-dir.denoising.oriImg}")
    private String denoisingOriImgLocation;

    // Redeyes 相关路径
    @Value("${file.upload-dir.redeyes.oriImg}")
    private String redeyesOriImgLocation;

    @Value("${file.upload-dir.redeyes.resImg}")
    private String redeyesResImgLocation;

    // Scratch 相关路径
    @Value("${file.upload-dir.scratch.oriImg}")
    private String scratchOriImgLocation;

    @Value("${file.upload-dir.scratch.resImg}")
    private String scratchResImgLocation;

    // stylegan 相关路径
    @Value("${file.upload-dir.stylegan.resImg}")
    private String styleganResImgLocation;

    // Segment 相关路径
    @Value("${file.upload-dir.segment.oriImg}")
    private String segmentOriImgLocation;

    @Value("${file.upload-dir.segment.resImg}")
    private String segmentResImgLocation;

    @Value("${file.segment.checkpoint}")
    private String segmentWeightFileLocation;

    //Ocr相关路径
    @Value("${file.upload-dir.ocr.oriImg}")
    private String ocrOriImgLocation;

    @Value("${file.upload-dir.ocr.resTxt}")
    private String ocrResTxtLocation;

    @Value("${file.upload-dir.ocr.transTxt}")
    private String ocrTransTxtLocation;

    @Value("${file.segment_ocr_translate.conda}")
    private String ocrCondaLocation;


    // root Bean
    @Bean(name = "rootPath")
    public Path rootPath() {
        return createDir(rootDir);
    }

    // ocrCondaPath
    @Bean(name = "ocrCondaPath")
    public Path ocrCondaPath(){ return createDir(ocrCondaLocation);}

    //segment_ocr_translate conda path Bean
    @Bean(name = "segment_ocr_translate_conda")
    public Path segment_ocr_translate_conda(){ return createDir(segment_ocr_translate_conda);}

    // stylegan Bean
    @Bean(name = "styleganResImgPath")
    public Path styleganResImgPath() {
        return createDir(styleganResImgLocation);
    }

    // Deepfill Beans
    @Bean(name = "deepfillSegImgPath")
    public Path deepfillSegImgPath() {
        return createDir(deepfillSegImgLocation);
    }

    @Bean(name = "deepfillOriImgPath")
    public Path deepfillOriImgPath() {
        return createDir(deepfillOriImgLocation);
    }

    @Bean(name = "deepfillFillImgPath")
    public Path deepfillFillImgPath() {
        return createDir(deepfillFillImgLocation);
    }

    // Denoising Beans
    @Bean(name = "denoisingResImgPath")
    public Path denoisingResImgPath() {
        return createDir(denoisingResImgLocation);
    }

    @Bean(name = "denoisingOriImgPath")
    public Path denoisingOriImgPath() {
        return createDir(denoisingOriImgLocation);
    }

    // Redeyes Beans
    @Bean(name = "redeyesOriImgPath")
    public Path redeyesOriImgPath() {
        return createDir(redeyesOriImgLocation);
    }

    @Bean(name = "redeyesResImgPath")
    public Path redeyesResImgPath() {
        return createDir(redeyesResImgLocation);
    }

    // Scratch Beans
    @Bean(name = "scratchOriImgPath")
    public Path scratchOriImgPath() {
        return createDir(scratchOriImgLocation);
    }

    @Bean(name = "scratchResImgPath")
    public Path scratchResImgPath() {
        return createDir(scratchResImgLocation);
    }

    // Segment  Bean
    @Bean(name = "segmentOriImgPath")
    public Path segmentOriImgPath() {
        return createDir(segmentOriImgLocation);
    }

    @Bean(name = "segmentResImgPath")
    public Path segmentResImgPath() {
        return createDir(segmentResImgLocation);
    }

    @Bean(name = "segmentWeightFileLocation")
    public Path segmentWeightFileLocation() {
        Path weightPath = Paths.get(segmentWeightFileLocation).toAbsolutePath().normalize();
        if (!Files.exists(weightPath)) {
            throw new IllegalStateException("权重文件不存在: " + weightPath);
        }
        return weightPath;
    }

    // Ocr Bean
    @Bean(name = "ocrOriImgLocation")
    public Path ocrOriImgLocation() {
        return createDir(ocrOriImgLocation);
    }

    @Bean(name = "ocrResTxtLocation")
    public Path ocrResTxtLocation() {
        return createDir(ocrResTxtLocation);
    }
    @Bean(name = "ocrTransTxtLocation")
    public Path ocrTransTxtLocation() {
        return createDir(ocrTransTxtLocation);
    }

    /**
     * 创建目录并返回Path对象
     */
    private Path createDir(String path) {
        Path dir = Paths.get(path).toAbsolutePath().normalize();
        try {
            Files.createDirectories(dir);
            return dir;
        } catch (IOException e) {
            throw new IllegalStateException("无法创建目录: " + path, e);
        }
    }
}