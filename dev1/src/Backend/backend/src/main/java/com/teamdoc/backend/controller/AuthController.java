package com.teamdoc.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
public class AuthController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${team.database.service.url}")
    private String dbServiceUrl;

    @PostMapping("/api/auth/register")
    public String register(@RequestBody Map<String, String> user) {
        String url = dbServiceUrl + "/api/user/register";
        return restTemplate.postForObject(url, user, String.class);
    }

    @PostMapping("/api/auth/login")
    public String login(@RequestBody Map<String, String> user) {
        String url = dbServiceUrl + "/api/user/login";
        return restTemplate.postForObject(url, user, String.class);
    }
}
