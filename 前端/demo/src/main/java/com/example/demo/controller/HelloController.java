package com.example.demo.controller;


import com.example.demo.DemoApplication;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.imageio.ImageIO;

import com.madgag.gif.fmsware.AnimatedGifEncoder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
class GifConverterController {

    @PostMapping(value = "/api/convert", produces = MediaType.IMAGE_GIF_VALUE)
    public ResponseEntity<?> convertToGif(
            @RequestParam("files") List<MultipartFile> files) throws IOException {

        // 检查文件连续性
        List<Integer> sequenceNumbers = new ArrayList<>();
        for (MultipartFile file : files) {
            String filename = file.getOriginalFilename();
            Matcher matcher = Pattern.compile("\\d+").matcher(filename);
            if (!matcher.find()) {
                return ResponseEntity.badRequest().body("文件名缺少序列号: " + filename);
            }
            sequenceNumbers.add(Integer.parseInt(matcher.group()));
        }

        // 验证连续性
        Collections.sort(sequenceNumbers);
        for (int i = 1; i < sequenceNumbers.size(); i++) {
            if (sequenceNumbers.get(i) - sequenceNumbers.get(i-1) != 1) {
                return ResponseEntity.badRequest().body("图片序列不连续");
            }
        }

        // 生成GIF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        encoder.start(outputStream);
        encoder.setDelay(200);
        encoder.setRepeat(0);

        for (MultipartFile file : files) {
            BufferedImage image = ImageIO.read(file.getInputStream());
            encoder.addFrame(image);
        }
        encoder.finish();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=animation.gif")
                .contentType(MediaType.IMAGE_GIF)
                .body(outputStream.toByteArray());
    }
}
@Controller
public class HelloController {
    @GetMapping("GIF")
    public String home() {
        return "GIF";  // 对应 templates/index.html
    }
    @GetMapping("GIFmem")
    public String memhome() {
        return "GIFmem";  // 对应 templates/index.html
    }
    @GetMapping("/Tourist_Homepage")
    public String showTourist_HomepagePage(){
        return "Tourist_Homepage";
    }
    @GetMapping("/Picture_cropping")
    public String showPicture_croppingPage(){
        return "Picture_cropping";
    }
    @GetMapping("/Picture_croppingmem")
    public String showPicture_croppingmemPage(){
        return "Picture_croppingmem";
    }
    @GetMapping("Conversion")
    public String showConversionPage() {
        return "Conversion";  // 对应 templates/index.html
    }
    @GetMapping("Conversionmem")
    public String showConversionmemPage() {
        return "Conversionmem";  // 对应 templates/index.html
    }
    @GetMapping("Compress")
    public String showCompressPage() {
        return "Compress";
    }
    @GetMapping("Compressmem")
    public String showCompressmemPage() {
        return "Compressmem";
    }
    @GetMapping("Test_Added")
    public String showTest_AddedPage() {
        return "Test_Added";
    }
    @GetMapping("Test_Addedmem")
    public String showTest_AddedmemPage() {
        return "Test_Addedmem";
    }
    @GetMapping("login")
    public String showloginPage() {
        return "login";
    }
    @GetMapping("Member_Homepage")
    public String showMember_HomepagePage() {
        return "Member_Homepage";
    }

    @GetMapping("redeyesTest1")
    public String showredeyesTest1Page() {
        return "redeyesTest1";
    }
    @GetMapping("stylegabtest2")
    public String showstylegabtest2Page() {
        return "stylegabtest2";
    }
    @GetMapping("scratchTest3")
    public String showscratchTest3Page() {
        return "scratchTest3";
    }
    @GetMapping("denoisingTest")
    public String showdenoisingPage() {
        return "denoisingTest";
    }
    @GetMapping("SAM")
    public String showSAMPage() {
        return "SAM";
    }
    @GetMapping("OCR")
    public String showOCRPage() {
        return "OCR";
    }
    @GetMapping("deepfill")
    public String showdeepfillPage() {
        return "deepfill";
    }
    @GetMapping("/register")
    public String showregisterPage() {return "register";}
    @GetMapping("/forget_pwd")
    public String showforgetpwdPage() {return "forget_pwd";}

    // 生成验证码
    @GetMapping("/captcha")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) {
        try {
        // 创建图片对象
        int width = 120;
        int height = 40;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // 绘制图形
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setFont(new Font("Arial", Font.BOLD, 30));

        // 生成随机验证码
        String characters = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random random = new Random();
        StringBuilder captcha = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            char c = characters.charAt(random.nextInt(characters.length()));
            captcha.append(c);
            g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            g.drawString(String.valueOf(c), 30 * i + 10, 30);
        }

        // 添加干扰线
        for (int i = 0; i < 5; i++) {
            g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            g.drawLine(random.nextInt(width), random.nextInt(height),
                    random.nextInt(width), random.nextInt(height));
        }
        g.dispose();

        // 保存验证码到Session
        request.getSession().setAttribute("CAPTCHA", captcha.toString());

        // 输出图片
        response.setContentType("image/png");
        OutputStream out = response.getOutputStream();
        ImageIO.write(image, "png", out);
        out.close();
        response.flushBuffer();
    }catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // 处理登录请求
    @PostMapping("/login")
    public Map<String, Object> login(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String captcha,
            HttpServletRequest request) {

        Map<String, Object> result = new HashMap<>();
//        System.out.println("username: " + username);

        // 验证验证码
        String sessionCaptcha = (String) request.getSession().getAttribute("CAPTCHA");
        if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(captcha)) {
            result.put("code", 400);
            result.put("message", "验证码错误");
            System.out.println("400");
            return result;
        }

        // 验证用户名密码（示例）
        if ("admin".equals(username) && "admin".equals(password)) {
            result.put("code", 200);
            result.put("message", "登录成功");
        } else {
            result.put("code", 401);
            result.put("message", "用户名或密码错误");
            //System.out.println("400");
        }

        // 清除已用验证码
        request.getSession().removeAttribute("CAPTCHA");
        return result;
    }
}