package com.doc.system.controller;

import com.doc.system.entity.User;
import com.doc.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        try {
            String token = userService.login(username, password);
            Long userId = Long.valueOf(token.substring("login-success-token-".length()));
            User user = userService.findById(userId);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("msg", "登录成功");
            result.put("data", token);
            result.put("role", user != null ? user.getRole() : "user");
            return result;
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 400);
            error.put("msg", e.getMessage());  // 例如"用户不存在"或"密码错误"
            return error;
        }
    }

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        userService.register(user.getUsername(), user.getPassword());
        return "注册成功";
    }
}
