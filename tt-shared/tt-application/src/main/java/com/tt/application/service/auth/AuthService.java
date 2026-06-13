package com.tt.application.service.auth;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tt.common.port.AdminAuthPort;
import com.tt.infrastructure.persistence.entity.FndUser;
import com.tt.infrastructure.persistence.mapper.FndUserMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements AdminAuthPort {

  private final FndUserMapper fndUserMapper;
  private final JdbcTemplate jdbc;
  private final PasswordEncoder passwordEncoder;

  public AuthService(FndUserMapper fndUserMapper, JdbcTemplate jdbc, PasswordEncoder passwordEncoder) {
    this.fndUserMapper = fndUserMapper;
    this.jdbc = jdbc;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public boolean hasAdminRole(Long userId) {
    Integer n =
        jdbc.queryForObject(
            "SELECT COUNT(1) FROM fnd_user_role ur "
                + "INNER JOIN fnd_role r ON r.id = ur.role_id "
                + "WHERE ur.user_id = ? AND r.code = 'admin'",
            Integer.class,
            userId);
    return n != null && n > 0;
  }

  public Long authenticate(String username, String password) {
    FndUser u = fndUserMapper.selectOne(Wrappers.<FndUser>lambdaQuery().eq(FndUser::getUsername, username));
    if (u == null) return null;
    if (!"active".equals(u.getStatus())) return null;
    if (!passwordEncoder.matches(password, u.getPasswordHash())) return null;
    return u.getId();
  }

  public List<String> rolesForUser(long userId) {
    return jdbc.query(
        "SELECT r.code FROM fnd_user_role ur "
            + "INNER JOIN fnd_role r ON r.id = ur.role_id WHERE ur.user_id = ?",
        (rs, i) -> rs.getString(1),
        userId);
  }

  public Map<String, Object> userById(long userId) {
    FndUser u = fndUserMapper.selectById(userId);
    if (u == null) return null;
    Map<String, Object> m = new HashMap<>();
    m.put("id", u.getId());
    m.put("username", u.getUsername());
    m.put("status", u.getStatus());
    return m;
  }
}
