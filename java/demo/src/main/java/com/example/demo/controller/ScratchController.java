package com.example.demo.controller;

import com.example.demo.pojo.ScratchBean;
import com.example.demo.service.ScratchService;
import com.example.demo.util.PathUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
public class ScratchController {
    private final Path oriImgPath;
    private final Path resImgPath;
    // 允许上传的图片类型
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Autowired
    private ScratchService scratchService;
    @Autowired
    public ScratchController(
            @Value("${file.upload-dir.scratch.oriImg}") String oriImgPath,
            @Value("${file.upload-dir.scratch.resImg}") String resImgPath) {
        this.oriImgPath = Paths.get(oriImgPath).toAbsolutePath().normalize();
        this.resImgPath = Paths.get(resImgPath).toAbsolutePath().normalize();
    }

    @PostMapping("/scratch/uploads")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        // 验证文件是否为空
        if (file.isEmpty()) {
            response.put("code", 0);
            response.put("msg", "上传文件不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        // 验证文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            response.put("code", 0);
            response.put("msg", "文件大小不能超过5MB");
            return ResponseEntity.badRequest().body(response);
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            response.put("code", 0);
            response.put("msg", "只支持JPG/PNG格式图片");
            return ResponseEntity.badRequest().body(response);
        }

        // 生成唯一文件名
        String newFilename = PathUtil.getNewFileName(oriImgPath.toString(),false);

        try {
            // 保存文件
            // 提取文件名序号
            String fileSequence = newFilename;
            // 在oriImgPath下创建名为fileSequence的文件夹
            Path oriImgDir = oriImgPath.resolve(fileSequence);
            Files.createDirectories(oriImgDir);

            // 在resImgPath下创建名为fileSequence的文件夹
            Path resImgDir = resImgPath.resolve(fileSequence);
            Files.createDirectories(resImgDir);

            Path targetLocation = Paths.get(oriImgPath + "/" + fileSequence + "/" +newFilename+".png");
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            

            // 构建响应
            response.put("code", 1);
            response.put("msg", "上传成功");
            response.put("oriImgUrl", "/scratch/origin_image/" + fileSequence + "/" + newFilename+".png");

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            response.put("code", 0);
            response.put("msg", "文件上传失败");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/scratch/dispose")
    public ResponseEntity<Map<String, Object>> disposeImage(@RequestBody Map<String, String> request){
        Map<String, Object> response = new HashMap<>();
        String oriImgUrl = request.get("oriImgUrl");
        try {
            // 获取请求参数
            if (oriImgUrl == null || oriImgUrl.isEmpty()) {
                response.put("code", 0);
                response.put("msg", "参数 oriImgUrl 不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 构造输出图片路径
            String fileName = PathUtil.getFileName(oriImgUrl);
            String fileSequence = fileName.substring(0, fileName.lastIndexOf('.'));
            oriImgUrl = oriImgPath+"/"+fileSequence;
            String resImgUrl = resImgPath+"/"+fileSequence;

            // 构造 ScratchBean
            ScratchBean scratchBean = new ScratchBean(oriImgUrl, resImgUrl);

            // 调用业务逻辑
            boolean success = scratchService.runStyleganScript(scratchBean);

            if (success) {
                // 成功响应
                response.put("code", 1);
                response.put("msg", "获取成功");
                Map<String, String> data = new HashMap<>();
                data.put("resImgUrl", resImgUrl);
                response.put("data", data);
                return ResponseEntity.ok(response);
            } else {
                // 失败响应
                response.put("code", 0);
                response.put("msg", "图片去划痕失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            // 异常处理
            log.error("图片去划痕处理异常", e);
            response.put("code", 0);
            response.put("msg", "服务器内部错误");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PostMapping("/scratch/download")
    public ResponseEntity<?> downloadImage(@RequestBody Map<String, String> request){
        String resImgUrl = request.get("resImgUrl");
        try {
            // 检查参数是否为空
            if (resImgUrl == null || resImgUrl.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 0,
                        "msg", "参数 resImgUrl 不能为空"
                ));
            }

            // 构造图片文件路径
            String imgDir = PathUtil.getFileName(resImgUrl);
            String image = resImgPath.toString()+"/"+imgDir+"/"+"final_output"+"/"+imgDir+".png";
            Path imagePath = Paths.get(image).toAbsolutePath().normalize();
            File imageFile = imagePath.toFile();

            // 检查文件是否存在
            if (!imageFile.exists() || !imageFile.isFile()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "code", 0,
                        "msg", "图片文件不存在"
                ));
            }

//            // 返回图片文件流
//            ResponseEntity<?> response = ResponseEntity.ok()
//            .header("Content-Disposition", "attachment; filename=\"" + imageFile.getName() + "\"")
//            .contentType(Files.probeContentType(imagePath) != null ?
//                    MediaType.parseMediaType(Files.probeContentType(imagePath)) :
//                    MediaType.APPLICATION_OCTET_STREAM)
//            .body(new InputStreamResource(new FileInputStream(imageFile)));
//
//            // 删除文件夹及其内容
//            deleteDirectory(Paths.get(resImgPath.toString(), imgDir));
//            deleteDirectory(Paths.get(oriImgPath.toString(), imgDir));
//
//            return response;

             // 返回图片文件流
             return ResponseEntity.ok()
                     .header("Content-Disposition", "attachment; filename=\"" + imageFile.getName() + "\"")
                     .contentType(Files.probeContentType(imagePath) != null ?
                             MediaType.parseMediaType(Files.probeContentType(imagePath)) :
                             MediaType.APPLICATION_OCTET_STREAM)
                     .body(new InputStreamResource(new FileInputStream(imageFile)));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "code", 0,
                    "msg", "服务器内部错误"
            ));
        }
    }

    // 删除文件夹及其内容
    private void deleteDirectory(Path directory) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file); // 删除文件
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir); // 删除空文件夹
                return FileVisitResult.CONTINUE;
            }
        });
    }
}

