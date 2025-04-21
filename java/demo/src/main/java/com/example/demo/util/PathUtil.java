package com.example.demo.util;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Paths;

public class PathUtil {
    public static String getNewFileName(MultipartFile file,String saveDir){
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")); // 获取扩展名

        int index = 1;
        String newFileName;
        do {
            newFileName = String.format("%08d", index) + extension;
            index++;
        } while (Files.exists(Paths.get(saveDir, newFileName)));

        return newFileName;
    }
}
