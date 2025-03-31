package com.example.demo.service;

import com.example.demo.pojo.dto.UserDto;
import org.springframework.stereotype.Service;

@Service              //Spring的bean
public class UserService implements IUserService {
    @Override
    public void add(UserDto user) {
        //调用数据访问类

    }
}
