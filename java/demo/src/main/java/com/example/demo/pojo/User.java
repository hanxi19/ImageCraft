package com.example.demo.pojo;

import jakarta.persistence.*;
import lombok.Data;

@Table(name="tb_user")       //把类映射成为数据库的表,表名字叫做tb_user
@Entity
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  //id自增
    @Column(name="user_id")
    private Integer userId;
    @Column(name="user_name")
    private String userName;
    @Column(name="password")
    private String password;
    @Column(name="email")
    private String email;


    public User(String email,String username, String password) {
        this.email = email;
        this.userName = username;
        this.password = password;
    }

    public User() {

    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
