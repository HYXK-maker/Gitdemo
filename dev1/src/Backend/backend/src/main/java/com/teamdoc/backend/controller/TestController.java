package com.teamdoc.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class TestController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${team.database.service.url}")
    private String dbServiceUrl;

    @GetMapping("/api/ping-db")
    public String pingDatabaseService() {
        // 这里假设队友服务根路径为 "/"，实际可替换为具体接口如 "/api/users"
        String url = dbServiceUrl + "/";
        try {
            String response = restTemplate.getForObject(url, String.class);
            return "成功调用队友服务，响应内容: " + response;
        } catch (Exception e) {
            return "调用队友服务失败: " + e.getMessage();
        }
    }
}
