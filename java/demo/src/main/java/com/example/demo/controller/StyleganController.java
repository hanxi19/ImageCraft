package com.example.demo.controller;

import com.example.demo.pojo.StyleganBean;
import com.example.demo.service.StyleganService;
import com.example.demo.service.styleganimpl.StyleganService1;
import com.example.demo.util.PathUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class StyleganController {
    private final Path resImgPath;

    @Autowired
    private StyleganService1 styleganService;

    @Autowired
    public StyleganController(
            @Value("${file.upload-dir.stylegan.resImg}") String resImgDir) {

        this.resImgPath = Paths.get(resImgDir).toAbsolutePath().normalize();
    }

    @GetMapping("/stylegan/gen")
    public ResponseEntity<Map<String, Object>> genImage(
            @RequestParam("seed") int seed,
            @RequestParam("trunc") float trunc,
            @RequestParam("network") int network){
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. 校验seed参数
            if (seed < 0) {
                response.put("code", 400);
                response.put("msg", "种子参数seed必须是非负整数");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // 2. 校验trunc参数
            if (trunc <= 0 || trunc > 1.0) {
                response.put("code", 400);
                response.put("msg", "图片质量trunc必须在(0,1.0]范围内");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // 3. 校验network参数
            if (network < 0 || network > 2) {
                response.put("code", 400);
                response.put("msg", "network参数必须是0(猫)、1(狗)或2(艺术人像)");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // 4. 生成图片
            String imgUrl = PathUtil.getNewFileName(resImgPath.toString());
            String fileName = PathUtil.getFileName(imgUrl);
            StyleganBean styleganBean = new StyleganBean(seed, trunc, network, imgUrl,fileName);
            boolean success = styleganService.runStyleganScript(styleganBean);

            if (!success) {
                response.put("code", 500);
                response.put("msg", "图片生成失败");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // 5. 返回成功响应
            response.put("code", 200);
            response.put("msg", "图片生成成功");
            response.put("imgUrl", imgUrl);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            log.error("图片生成异常", e);
            response.put("code", 500);
            response.put("msg", "服务器内部错误");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
