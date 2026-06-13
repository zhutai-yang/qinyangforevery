package com.tt.api.controller;

import com.tt.api.web.AdminUserId;
import com.tt.infrastructure.security.JwtService;
import com.tt.application.service.auth.AuthService;
import com.tt.application.service.governance.LoginRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
public class AuthController {

  private final AuthService authService;
  private final LoginRateLimiter rateLimiter;
  private final JwtService jwtService;

  public AuthController(AuthService authService, LoginRateLimiter rateLimiter, JwtService jwtService) {
    this.authService = authService;
    this.rateLimiter = rateLimiter;
    this.jwtService = jwtService;
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpServletRequest request) {
    String ip = request.getRemoteAddr();
    if (!rateLimiter.allow(ip)) {
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
          .body(Map.of("code", "RATE_LIMIT", "message", "登录尝试过多"));
    }
    String username = body.get("username");
    String password = body.get("password");
    if (username == null || password == null || username.isEmpty()) {
      return ResponseEntity.badRequest()
          .body(Map.of("code", "BAD_REQUEST", "message", "请输入用户名和密码"));
    }
    Long id = authService.authenticate(username, password);
    if (id == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("code", "AUTH_FAILED", "message", "用户名或密码错误"));
    }
    String token = jwtService.createToken(id, username);
    List<String> roles = authService.rolesForUser(id);
    Map<String, Object> res = new HashMap<>();
    res.put("ok", true);
    res.put("token", token);
    res.put("user", Map.of("id", id, "username", username));
    res.put("roles", roles);
    return ResponseEntity.ok(res);
  }

  @PostMapping("/logout")
  public Map<String, Object> logout() {
    return Map.of("ok", true);
  }

  @GetMapping("/me")
  public Map<String, Object> me() {
    long uid = AdminUserId.get();
    Map<String, Object> user = authService.userById(uid);
    if (user == null) return Map.of("user", null);
    return Map.of("user", user, "roles", authService.rolesForUser(uid));
  }
}
