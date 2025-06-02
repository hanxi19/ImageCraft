package com.example.demo.controller;

import com.example.demo.pojo.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping("/index")
    public String showIndexPage(){
        return "Tourist_Homepage.html";
    }


    @GetMapping("/logintest")
    public String showLoginPage() {
        return "login.html";
    }
    //
    @PostMapping("/logintest")
    public String processLogin(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               @RequestParam String captcha,HttpServletRequest request,
                               Model model) {


        //User user = userRepository.findByUserName(username);

        System.out.println("输入的用户名: " + username);
        System.out.println("输入的密码: " + password);
//        Map<String, Object> response = new HashMap<>();
        User user = userRepository.findByUserName(username);
        Map<String, Object> result = new HashMap<>();
        String sessionCaptcha = (String) request.getSession().getAttribute("CAPTCHA");
        if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(captcha)) {
            result.put("code", 400);
            result.put("message", "验证码错误");
            //将错误信息传递给前端
            model.addAttribute("error", result.get("message"));
            System.out.println(result);
            return "login";

        }
        else {
            System.out.println("验证码正确");
            if (user != null && user.getPassword().equals(password)) {

                if (user.getUserName().equals("root")) {
                    return "admin";

                }
                else {
//                    response.put("code", 200);
//                    response.put("msg", "登录成功");
                    result.put("code", 200);
                    result.put("message", "success");
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("email", user.getEmail());
                    userData.put("username", user.getUserName());
                    userData.put("password", user.getPassword());
//                    result.put("data", userData);
                    //return new ResponseEntity<>(response, HttpStatus.OK);
                    //model.addAttribute("response", response);
                    model.addAttribute("result", result);
                    return "Member_Homepage";
                }

            }

            else {
                result.put("code", 0);
                result.put("message", "用户名或密码错误");
                result.put("data", null);
//                model.addAttribute("response", result);
                //将错误信息传递给前端
                model.addAttribute("error", result.get("message"));
                return "login";

            }
        }


    }

//    @GetMapping("/register")
//    public String showRegisterPage(Model model) {
//        model.addAttribute("user", new User());
//        return "register.html";
//    }

    //    @PostMapping("/register")
//    public ResponseEntity<Map<String, Object>> processRegister(@RequestParam("email") String email,
//                                  @RequestParam("username") String username,
//                                  @RequestParam("password") String password,
//                                  @RequestParam("password2") String password2,
//                                  Model model) {
//        //User newUser = new User(email,username, password);
//        //userRepository.save(newUser);
//
//        Map<String, Object> response = new HashMap<>();
//
//
//        if (!password.equals(password2)) {
//            response.put("code", 0);
//            response.put("msg", "注册失败：两次输入的密码不一致");
//            response.put("data", null);
//            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//        }
//
//        try {
//
//            User newUser = new User(email, username, password);
//            userRepository.save(newUser);
//            System.out.println("邮箱：" + email);
//            System.out.println("输入的用户名: " + username);
//            System.out.println("输入的密码: " + password);
//            model.addAttribute("message", "注册成功");
//            HttpHeaders headers = new HttpHeaders();
//            headers.setLocation(new URI("/logintest"));
//            response.put("code", 1);
//            response.put("msg", "注册成功");
//            response.put("data", null);
//            return new ResponseEntity<>(response,headers,HttpStatus.OK);
//        } catch (Exception e) {
//            response.put("code", 0);
//            response.put("msg", "注册失败：" + e.getMessage());
//            response.put("data", null);
//            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//
//
//    }
    @PostMapping("/register")
    public String registerUser(@RequestParam String email,
                               @RequestParam String username,
                               @RequestParam String password,
                               Model model) {
        try {
            User newUser = new User(email, username, password);
            userRepository.save(newUser);
            System.out.println("邮箱：" + email);
            System.out.println("输入的用户名: " + username);
            System.out.println("输入的密码: " + password);

            // 注册成功后重定向到登录页面
            return "redirect:/logintest";
        } catch (Exception e) {
            model.addAttribute("error", "注册失败：" + e.getMessage());
            return "register"; // 返回注册页面并显示错误信息
        }
    }



//    @GetMapping("/forget_pwd")
//    public String showForgetPasswordPage() {
//        return "forget_pwd";
//    }

    @PostMapping("/sendResetPasswordEmail")
    public ResponseEntity sendResetPasswordEmail(@RequestParam String emailAddress) {
        User user = userRepository.findByEmail(emailAddress);
        if(user != null) {
            userService.sendResetPasswordEmail(emailAddress);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/resetPassword")
    public ResponseEntity resetPassword(@RequestParam String emailAddress, @RequestParam String verificationCode, @RequestParam String newPassword) {
        if (userService.validateVerificationCode(emailAddress, verificationCode)) {
//            userService.resetPassword(emailAddress, newPassword);
            User user = userRepository.findByEmail(emailAddress);
            if(user != null) {
                user.setPassword(newPassword);
                userRepository.save(user);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


//    @GetMapping("/captcha")
//    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) {
//        try {
//            // 创建图片对象
//            int width = 120;
//            int height = 40;
//            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//
//            // 绘制图形
//            Graphics2D g = image.createGraphics();
//            g.setColor(Color.WHITE);
//            g.fillRect(0, 0, width, height);
//            g.setFont(new Font("Arial", Font.BOLD, 30));
//
//            // 生成随机验证码
//            String characters = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
//            Random random = new Random();
//            StringBuilder captcha = new StringBuilder();
//            for (int i = 0; i < 4; i++) {
//                char c = characters.charAt(random.nextInt(characters.length()));
//                captcha.append(c);
//                g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
//                g.drawString(String.valueOf(c), 30 * i + 10, 30);
//            }
//
//            // 添加干扰线
//            for (int i = 0; i < 5; i++) {
//                g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
//                g.drawLine(random.nextInt(width), random.nextInt(height),
//                        random.nextInt(width), random.nextInt(height));
//            }
//            g.dispose();
//
//            // 保存验证码到Session
//            request.getSession().setAttribute("CAPTCHA", captcha.toString());
//
//            // 输出图片
//            response.setContentType("image/png");
//            OutputStream out = response.getOutputStream();
//            ImageIO.write(image, "png", out);
//            out.close();
//            response.flushBuffer();
//        }catch (Exception e) {
//            e.printStackTrace();
//            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//        }
//    }

    // 处理登录请求
//    @PostMapping("/login")
//    public Map<String, Object> login(
//            @RequestParam String username,
//            @RequestParam String password,
//            @RequestParam String captcha,
//            HttpServletRequest request) {
//
//        Map<String, Object> result = new HashMap<>();
//        System.out.println("输入的用户名: " + username);
//        System.out.println("输入的密码: " + password);
//        // 验证验证码
//        String sessionCaptcha = (String) request.getSession().getAttribute("CAPTCHA");
//        if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(captcha)) {
//            result.put("code", 400);
//            result.put("message", "验证码错误");
//            System.out.println("验证码错误");
//            return result;
//        }
//
//        // 验证用户名密码（示例）
//        if ("admin".equals(username) && "admin".equals(password)) {
//            result.put("code", 200);
//            result.put("message", "登录成功");
//        } else {
//            result.put("code", 401);
//            result.put("message", "用户名或密码错误");
//        }
//
//        // 清除已用验证码
//        request.getSession().removeAttribute("CAPTCHA");
//        return result;
//    }

}