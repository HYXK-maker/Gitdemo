package com.doc.system.controller;

import com.doc.system.common.Result;
import com.doc.system.entity.User;
import com.doc.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<String> login(@RequestBody User user) {
        String token = userService.login(user.getUsername(), user.getPassword());
        return Result.success(token);
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        // 调用 UserService 的注册方法
        userService.register(user.getUsername(), user.getPassword());
        return Result.success("注册成功");
    }
}