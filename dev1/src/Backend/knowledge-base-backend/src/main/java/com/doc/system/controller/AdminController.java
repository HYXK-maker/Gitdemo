
package com.doc.system.controller;

import com.doc.system.entity.User;
import com.doc.system.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserMapper userMapper;

    private boolean isAdmin(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) return false;
        Long userId = Long.valueOf(userIdStr);
        User user = userMapper.findById(userId);
        return user != null && "admin".equals(user.getRole());
    }

    @GetMapping("/users")
    public Map<String, Object> listUsers(HttpServletRequest request) {
        if (!isAdmin(request)) {
            Map<String, Object> err = new HashMap<>();
            err.put("code", 403);
            err.put("msg", "无权限");
            return err;
        }
        List<User> allUsers = userMapper.findAll();
        // 只过滤掉 username='admin' 的用户，其他用户都显示
        List<User> users = allUsers.stream()
                .filter(u -> !"admin".equals(u.getUsername()))
                .collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", users);
        return result;
    }

    @PostMapping("/changeRole")
    public Map<String, Object> changeRole(@RequestBody Map<String, String> params, HttpServletRequest request) {
        if (!isAdmin(request)) {
            Map<String, Object> err = new HashMap<>();
            err.put("code", 403);
            err.put("msg", "无权限");
            return err;
        }
        Long id = Long.valueOf(params.get("id"));
        String role = params.get("role");
        userMapper.updateRole(id, role);

        Long currentUserId = Long.valueOf(request.getHeader("X-User-Id"));
        boolean selfDemoted = id.equals(currentUserId) && "user".equals(role);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "角色修改成功");
        result.put("selfDemoted", selfDemoted);
        return result;
    }

    @PostMapping("/toggleStatus")
    public Map<String, Object> toggleStatus(@RequestBody Map<String, String> params, HttpServletRequest request) {
        if (!isAdmin(request)) {
            Map<String, Object> err = new HashMap<>();
            err.put("code", 403);
            err.put("msg", "无权限");
            return err;
        }
        Long id = Long.valueOf(params.get("id"));
        Integer status = Integer.valueOf(params.get("status"));
        userMapper.updateStatus(id, status);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "状态修改成功");
        return result;
    }

    @PostMapping("/deleteUser")
    public Map<String, Object> deleteUser(@RequestBody Map<String, String> params, HttpServletRequest request) {
        if (!isAdmin(request)) {
            Map<String, Object> err = new HashMap<>();
            err.put("code", 403);
            err.put("msg", "无权限");
            return err;
        }
        Long id = Long.valueOf(params.get("id"));
        userMapper.deleteById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "用户已删除");
        return result;
    }
}
