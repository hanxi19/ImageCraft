package com.example.demo.service;

import com.example.demo.pojo.User;
import com.example.demo.pojo.dto.UserDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service              //Spring的bean
public class UserService implements IUserService {
    @Override
    public void add(UserDto user) {
        //调用数据访问类

    }

    private static final String RESET_PASSWORD_CODE_PREFIX = "reset_password:code:";

    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;

    public UserService(EmailService emailService, RedisTemplate<String, String> redisTemplate) {
        this.emailService = emailService;
        this.redisTemplate = redisTemplate;
    }

    public void sendResetPasswordEmail(String emailAddress) {
        // 1. 生成验证码
        String verificationCode = emailService.generateVerificationCode();

        // 2. 将验证码存储到 Redis
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set(RESET_PASSWORD_CODE_PREFIX + emailAddress, verificationCode, 10, TimeUnit.MINUTES);

        // 3. 发送邮件
        String emailSubject = "重置密码";
        String emailContent = "您的验证码为: " + verificationCode;
        emailService.sendSimpleEmail(emailAddress, emailSubject, emailContent);
    }
    public boolean validateVerificationCode(String emailAddress, String inputCode) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String storedCode = ops.get(RESET_PASSWORD_CODE_PREFIX + emailAddress);
        return inputCode.equals(storedCode);
    }

//    public void resetPassword(String emailAddress, String newPassword) {
//        // 在这里实现重置密码的逻辑
//        // 从数据库中获取用户对象
//        User user = userRepository.findByUserName(username)
//                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
//
//        // 这里假设你的 User 实体类有一个 setPassword 方法来设置密码
//        user.setPassword(newPassword);
//
//        // 将修改后的用户对象保存到数据库
//        userRepository.save(user);
//    }


}
