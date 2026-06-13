package com.tt.application.service.governance;

import com.tt.common.exception.BadRequestException;
import com.tt.common.exception.NotFoundException;
import com.tt.common.support.JdbcSupport;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GovernanceAdminService {

  private final JdbcTemplate jdbc;
  private final AuditService audit;
  private final PasswordEncoder passwordEncoder;

  public GovernanceAdminService(
      JdbcTemplate jdbc, AuditService audit, PasswordEncoder passwordEncoder) {
    this.jdbc = jdbc;
    this.audit = audit;
    this.passwordEncoder = passwordEncoder;
  }

  public Map<String, Object> getDict(String typeCode) {
    List<Map<String, Object>> t =
        jdbc.queryForList("SELECT id FROM fnd_dict_type WHERE code = ?", typeCode);
    if (t.isEmpty()) return Map.of("type", null, "items", List.of());
    long tid = ((Number) t.get(0).get("id")).longValue();
    List<Map<String, Object>> items =
        jdbc.queryForList(
            "SELECT id, code, label, sort_order FROM fnd_dict_item WHERE type_id = ? ORDER BY sort_order, id",
            tid);
    return Map.of("type", typeCode, "items", items);
  }

  @SuppressWarnings("unchecked")
  public void putDict(String typeCode, Map<String, Object> body) {
    List<Map<String, Object>> items = (List<Map<String, Object>>) body.getOrDefault("items", List.of());
    List<Map<String, Object>> t =
        jdbc.queryForList("SELECT id FROM fnd_dict_type WHERE code = ?", typeCode);
    if (t.isEmpty()) throw new NotFoundException("字典类型不存在");
    long tid = ((Number) t.get(0).get("id")).longValue();
    jdbc.update("DELETE FROM fnd_dict_item WHERE type_id = ?", tid);
    for (Map<String, Object> it : items) {
      jdbc.update(
          "INSERT INTO fnd_dict_item (type_id, code, label, sort_order) VALUES (?,?,?,?)",
          tid,
          it.get("code"),
          it.get("label"),
          it.get("sort_order") != null ? ((Number) it.get("sort_order")).intValue() : 0);
    }
  }

  public Map<String, Object> auditLogs(int page, int pageSize) {
    int size = JdbcSupport.pageSize(pageSize, 100);
    int off = (JdbcSupport.page(page) - 1) * size;
    int total = jdbc.queryForObject("SELECT COUNT(1) FROM gov_audit_log", Integer.class);
    List<Map<String, Object>> list =
        jdbc.queryForList(
            "SELECT l.*, u.username FROM gov_audit_log l LEFT JOIN fnd_user u ON u.id = l.user_id "
                + "ORDER BY l.id DESC LIMIT ? OFFSET ?",
            size,
            off);
    return Map.of("list", list, "total", total, "page", JdbcSupport.page(page), "pageSize", size);
  }

  public Map<String, Object> listUsers(int page, int pageSize) {
    int size = JdbcSupport.pageSize(pageSize, 100);
    int off = (JdbcSupport.page(page) - 1) * size;
    int total = jdbc.queryForObject("SELECT COUNT(1) FROM fnd_user", Integer.class);
    List<Map<String, Object>> list =
        jdbc.queryForList(
            "SELECT u.id, u.username, u.status, u.created_at, "
                + "GROUP_CONCAT(r.code) AS role_codes FROM fnd_user u "
                + "LEFT JOIN fnd_user_role ur ON ur.user_id = u.id "
                + "LEFT JOIN fnd_role r ON r.id = ur.role_id "
                + "GROUP BY u.id, u.username, u.status, u.created_at "
                + "ORDER BY u.id DESC LIMIT ? OFFSET ?",
            size,
            off);
    return Map.of("list", list, "total", total, "page", JdbcSupport.page(page), "pageSize", size);
  }

  @SuppressWarnings("unchecked")
  @Transactional
  public Map<String, Object> createUser(Map<String, Object> body, long adminId) {
    String username = body.get("username") != null ? body.get("username").toString().trim() : "";
    String password = body.get("password") != null ? body.get("password").toString() : "";
    if (username.isEmpty()) throw new BadRequestException("用户名不能为空");
    if (password.isEmpty()) throw new BadRequestException("密码不能为空");
    Integer exists =
        jdbc.queryForObject("SELECT COUNT(1) FROM fnd_user WHERE username = ?", Integer.class, username);
    if (exists != null && exists > 0) throw new BadRequestException("用户名已存在");
    String hash = passwordEncoder.encode(password);
    String status = body.get("status") != null ? body.get("status").toString() : "active";
    GeneratedKeyHolder kh = new GeneratedKeyHolder();
    jdbc.update(
        conn -> {
          PreparedStatement ps =
              conn.prepareStatement(
                  "INSERT INTO fnd_user (username, password_hash, status) VALUES (?,?,?)",
                  Statement.RETURN_GENERATED_KEYS);
          ps.setString(1, username);
          ps.setString(2, hash);
          ps.setString(3, status);
          return ps;
        },
        kh);
    long userId = kh.getKey().longValue();
    List<String> roleCodes = (List<String>) body.get("role_codes");
    if (roleCodes != null && !roleCodes.isEmpty()) {
      for (String code : roleCodes) {
        List<Map<String, Object>> roles =
            jdbc.queryForList("SELECT id FROM fnd_role WHERE code = ?", code);
        if (!roles.isEmpty()) {
          long roleId = ((Number) roles.get(0).get("id")).longValue();
          jdbc.update("INSERT INTO fnd_user_role (user_id, role_id) VALUES (?,?)", userId, roleId);
        }
      }
    }
    audit.write(adminId, "create", "user", userId, username);
    return getUserById(userId);
  }

  public Map<String, Object> getUserById(long id) {
    List<Map<String, Object>> rows =
        jdbc.queryForList("SELECT id, username, status, created_at FROM fnd_user WHERE id = ?", id);
    if (rows.isEmpty()) throw new NotFoundException("用户不存在");
    Map<String, Object> row = new LinkedHashMap<>(rows.get(0));
    List<String> codes =
        jdbc.query(
            "SELECT r.code FROM fnd_user_role ur INNER JOIN fnd_role r ON r.id = ur.role_id WHERE ur.user_id = ?",
            (rs, i) -> rs.getString(1),
            id);
    row.put("role_codes", codes);
    return row;
  }

  @SuppressWarnings("unchecked")
  @Transactional
  public Map<String, Object> updateUser(long id, Map<String, Object> body, long adminId) {
    List<Map<String, Object>> u = jdbc.queryForList("SELECT id FROM fnd_user WHERE id = ?", id);
    if (u.isEmpty()) throw new NotFoundException("用户不存在");
    if (body.get("status") != null) {
      jdbc.update(
          "UPDATE fnd_user SET status = ?, updated_at = CURRENT_TIMESTAMP(3) WHERE id = ?",
          body.get("status").toString(),
          id);
    }
    if (body.get("role_codes") != null) {
      jdbc.update("DELETE FROM fnd_user_role WHERE user_id = ?", id);
      List<String> roleCodes = (List<String>) body.get("role_codes");
      for (String code : roleCodes) {
        List<Map<String, Object>> roles =
            jdbc.queryForList("SELECT id FROM fnd_role WHERE code = ?", code);
        if (!roles.isEmpty()) {
          long roleId = ((Number) roles.get(0).get("id")).longValue();
          jdbc.update("INSERT INTO fnd_user_role (user_id, role_id) VALUES (?,?)", id, roleId);
        }
      }
    }
    audit.write(adminId, "update", "user", id, null);
    return getUserById(id);
  }

  @Transactional
  public Map<String, Object> updateUserPassword(long id, Map<String, Object> body, long adminId) {
    List<Map<String, Object>> u = jdbc.queryForList("SELECT id FROM fnd_user WHERE id = ?", id);
    if (u.isEmpty()) throw new NotFoundException("用户不存在");
    String newPassword = body.get("new_password") != null ? body.get("new_password").toString() : "";
    if (newPassword.isEmpty()) throw new BadRequestException("新密码不能为空");
    String hash = passwordEncoder.encode(newPassword);
    jdbc.update(
        "UPDATE fnd_user SET password_hash = ?, updated_at = CURRENT_TIMESTAMP(3) WHERE id = ?",
        hash,
        id);
    audit.write(adminId, "update_password", "user", id, null);
    return Map.of("ok", true);
  }
}
