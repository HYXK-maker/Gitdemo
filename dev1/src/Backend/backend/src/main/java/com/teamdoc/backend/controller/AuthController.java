package com.teamdoc.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${team.database.service.url}")
    private String dbServiceUrl;

    @PostMapping("/api/auth/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> user) {
        String url = dbServiceUrl + "/api/user/register";
        String result = restTemplate.postForObject(url, user, String.class);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("msg", "注册成功");
        response.put("data", result);
        return ResponseEntity.ok(response);
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/api/auth/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> user) {
        String url = dbServiceUrl + "/api/user/login";
        // 8080 登录接口返回 JSON 对象，包含 data 字段
        Map<String, Object> result = restTemplate.postForObject(url, user, Map.class);
        String token = null;
        Long userId = null;
        if (result != null && result.get("data") != null) {
            token = result.get("data").toString();
            // token格式: "login-success-token-2"
            if (token.startsWith("login-success-token-")) {
                String idPart = token.substring("login-success-token-".length());
                userId = Long.valueOf(idPart);
            }
        }
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("msg", "登录成功");
        response.put("token", token);
        response.put("userId", userId);
        return ResponseEntity.ok(response);
    }
}
