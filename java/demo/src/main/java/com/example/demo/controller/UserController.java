package com.example.demo.controller;

import com.example.demo.pojo.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/index")
    public String showIndexPage(){
        return "index.html";
    }


    @GetMapping("/login")
    public String showLoginPage() {
        return "logintest.html";
    }
    //
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> processLogin(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               Model model) {
        //User user = userRepository.findByUserName(username);

        System.out.println("输入的用户名: " + username);
        System.out.println("输入的密码: " + password);
        Map<String, Object> response = new HashMap<>();
        User user = userRepository.findByUserName(username);
        if (user != null && user.getPassword().equals(password)) {
            response.put("code", 1);
            response.put("msg", "登录成功");
            Map<String, Object> userData = new HashMap<>();
            userData.put("email", user.getEmail());
            userData.put("username", user.getUserName());
            userData.put("password", user.getPassword());
            response.put("data", userData);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } else {
            response.put("code", 0);
            response.put("msg", "用户名或密码错误");
            response.put("data", null);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
//        if (user != null && user.getPassword().equals(password)) {
//            model.addAttribute("message", "登录成功");
//            return "success.html";
//        } else {
//            model.addAttribute("message", "用户名或密码错误");
//            return "logintest.html";
//        }
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register.html";
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> processRegister(@RequestParam("email") String email,
                                  @RequestParam("username") String username,
                                  @RequestParam("password") String password,
                                  Model model) {
        //User newUser = new User(email,username, password);
        //userRepository.save(newUser);

        Map<String, Object> response = new HashMap<>();
        try {
            User newUser = new User(email, username, password);
            userRepository.save(newUser);
            System.out.println("邮箱：" + email);
            System.out.println("输入的用户名: " + username);
            System.out.println("输入的密码: " + password);
            model.addAttribute("message", "注册成功");
            response.put("code", 1);
            response.put("msg", "注册成功");
            response.put("data", null);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("code", 0);
            response.put("msg", "注册失败：" + e.getMessage());
            response.put("data", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

//        System.out.println("邮箱：" + email);
//        System.out.println("输入的用户名: " + username);
//        System.out.println("输入的密码: " + password);
//        model.addAttribute("message", "注册成功");
        //return "success.html";
    }

}