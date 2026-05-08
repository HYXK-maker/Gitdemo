package com.doc.system.service;

import com.doc.system.entity.User;
import com.doc.system.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public String login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("密码错误");
        }
        return "login-success-token-" + user.getId();
    }

    // 添加注册方法
    public void register(String username, String password) {
        // 1. 检查用户名是否已存在
        User existingUser = userMapper.findByUsername(username);
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // 注意：实际项目中需要加密

        // 3. 保存到数据库
        userMapper.insert(user);
    }
}