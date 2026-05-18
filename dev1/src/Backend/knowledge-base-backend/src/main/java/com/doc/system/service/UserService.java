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
        // 检查账号是否被禁用（status=1 表示禁用）
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new RuntimeException("账号已被禁用");
        }
        return "login-success-token-" + user.getId();
    }

    public void register(String username, String password) {
        User existingUser = userMapper.findByUsername(username);
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        // status 使用数据库默认值 0（正常）
        userMapper.insert(user);
    }

    public User findById(Long id) {
        return userMapper.findById(id);
    }
}
