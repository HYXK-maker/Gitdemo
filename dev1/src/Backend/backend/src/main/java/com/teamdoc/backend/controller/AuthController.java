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
        Map<String, Object> result = restTemplate.postForObject(url, user, Map.class);
        // 检查8080返回的code是否为200
        if (result != null && result.get("code") != null && !result.get("code").equals(200)) {
            // 直接透传错误信息给前端
            return ResponseEntity.ok(result);
        }
        String token = null;
        Long userId = null;
        String role = "user";
        if (result != null) {
            if (result.get("data") != null) {
                token = result.get("data").toString();
                if (token.startsWith("login-success-token-")) {
                    String idPart = token.substring("login-success-token-".length());
                    userId = Long.valueOf(idPart);
                }
            }
            if (result.get("role") != null) {
                role = result.get("role").toString();
            }
        }
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("msg", "登录成功");
        response.put("token", token);
        response.put("userId", userId);
        response.put("role", role);
        return ResponseEntity.ok(response);
    }
}
