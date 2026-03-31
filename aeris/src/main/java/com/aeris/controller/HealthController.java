package com.aeris.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {
  @GetMapping("/health")
  public Map<String, String> health() {
    return Map.of("message", "サーバーは元気です。");
  }

  @GetMapping("/health/user")
  public String userHealth(@RequestParam String name, @RequestParam int age) {
    return "User: " + name + ", Age: " + age;
  }
}
