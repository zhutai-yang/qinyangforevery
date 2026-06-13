package com.tt.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tt.common.port.AdminAuthPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final AdminAuthPort adminAuthPort;
  private final ObjectMapper objectMapper;

  public JwtAuthFilter(JwtService jwtService, AdminAuthPort adminAuthPort, ObjectMapper objectMapper) {
    this.jwtService = jwtService;
    this.adminAuthPort = adminAuthPort;
    this.objectMapper = objectMapper;
  }

  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    String path = request.getRequestURI();
    if (path == null) return true;
    if (path.startsWith("/api/public/") || "/api/health".equals(path)) return true;
    if (path.equals("/api/admin/auth/login")) return true;
    return !path.startsWith("/api/admin/");
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
    String token = null;
    if (auth != null && auth.startsWith("Bearer ")) {
      token = auth.substring(7).trim();
    }
    if (token == null || token.isEmpty()) {
      writeJson(response, 401, Map.of("code", "UNAUTHORIZED", "message", "请先登录（Authorization: Bearer token）"));
      return;
    }
    if (!jwtService.validate(token)) {
      writeJson(response, 401, Map.of("code", "UNAUTHORIZED", "message", "登录已失效，请重新登录"));
      return;
    }
    Long uid;
    try {
      uid = jwtService.parseUserId(token);
    } catch (Exception e) {
      writeJson(response, 401, Map.of("code", "UNAUTHORIZED", "message", "Token 无效"));
      return;
    }
    if (!adminAuthPort.hasAdminRole(uid)) {
      writeJson(response, 403, Map.of("code", "FORBIDDEN", "message", "需要管理员角色"));
      return;
    }
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
            uid, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    SecurityContextHolder.getContext().setAuthentication(authentication);
    filterChain.doFilter(request, response);
  }

  private void writeJson(HttpServletResponse response, int status, Map<String, String> body)
      throws IOException {
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    objectMapper.writeValue(response.getWriter(), body);
  }
}
