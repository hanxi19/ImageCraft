package com.example.demo.controller;
import com.example.demo.pojo.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/users")
public class AdminUserController {
    private static final Logger logger = LoggerFactory.getLogger(AdminUserController.class);

    @Autowired
    private UserRepository userRepository;

    // 获取所有用户
    @GetMapping
    public ResponseEntity<List<User>> getUsers() {
        List<User> users = userRepository.findAll();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    // 搜索用户
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam("type") String type,
                                                  @RequestParam("keyword") String keyword) {
        List<User> users;
        if ("username".equals(type)) {
            users = userRepository.findByUserNameContaining(keyword);
        } else if ("email".equals(type)) {
            users = userRepository.findByEmailContaining(keyword);
        } else {
            users = userRepository.findAll();
        }
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    // 删除用户
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
//        userRepository.deleteById(id);
//        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//    }
    @DeleteMapping("/{userId}")
    @Transactional
    public ResponseEntity<Void> deleteUser(@PathVariable("userId") Long id) {
        try {
            if (userRepository.existsById(id)) {
                userRepository.deleteById(id);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            // 出现异常时，事务会自动回滚
            logger.error("删除用户时出现异常，用户 ID: {}", id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}