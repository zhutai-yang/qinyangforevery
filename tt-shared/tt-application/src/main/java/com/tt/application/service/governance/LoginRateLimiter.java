package com.tt.application.service.governance;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 管理端登录限流：按 IP 在时间窗口内累计尝试次数，数据存 MySQL（不依赖 Redis）。
 */
@Component
public class LoginRateLimiter {

  private static final int MAX_ATTEMPTS = 30;
  private static final long WINDOW_MS = Duration.ofMinutes(15).toMillis();

  private final JdbcTemplate jdbc;

  public LoginRateLimiter(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Transactional
  public boolean allow(String ip) {
    if (ip == null || ip.isEmpty()) {
      ip = "unknown";
    }
    long now = System.currentTimeMillis();
    List<Map<String, Object>> rows =
        jdbc.queryForList("SELECT attempt_count, window_start_ms FROM sys_login_rate WHERE ip = ?", ip);
    int count;
    if (rows.isEmpty()) {
      jdbc.update(
          "INSERT INTO sys_login_rate (ip, attempt_count, window_start_ms) VALUES (?, 1, ?)", ip, now);
      count = 1;
    } else {
      int prev = ((Number) rows.get(0).get("attempt_count")).intValue();
      long start = ((Number) rows.get(0).get("window_start_ms")).longValue();
      if (now - start > WINDOW_MS) {
        jdbc.update(
            "UPDATE sys_login_rate SET attempt_count = 1, window_start_ms = ? WHERE ip = ?", now, ip);
        count = 1;
      } else {
        count = prev + 1;
        jdbc.update("UPDATE sys_login_rate SET attempt_count = ? WHERE ip = ?", count, ip);
      }
    }
    return count <= MAX_ATTEMPTS;
  }
}
