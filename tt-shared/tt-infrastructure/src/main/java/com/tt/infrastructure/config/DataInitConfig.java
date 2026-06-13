package com.tt.infrastructure.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitConfig implements CommandLineRunner {

  private final JdbcTemplate jdbc;
  private final PasswordEncoder passwordEncoder;

  public DataInitConfig(JdbcTemplate jdbc, PasswordEncoder passwordEncoder) {
    this.jdbc = jdbc;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(String... args) {
    String pwd = System.getenv("ADMIN_INITIAL_PASSWORD");
    if (pwd == null || pwd.isEmpty()) pwd = "admin123";
    String hash = passwordEncoder.encode(pwd);

    if (jdbc.queryForObject("SELECT COUNT(1) FROM fnd_role WHERE code = 'admin'", Integer.class) == 0) {
      jdbc.update("INSERT INTO fnd_role (code, name) VALUES ('admin', '管理员')");
    }
    long roleId = jdbc.queryForObject("SELECT id FROM fnd_role WHERE code = 'admin'", Long.class);

    if (jdbc.queryForObject("SELECT COUNT(1) FROM fnd_dict_type WHERE code = 'event_level'", Integer.class) == 0) {
      jdbc.update("INSERT INTO fnd_dict_type (code, name) VALUES ('event_level', '赛事级别')");
    }
    Long typeId = jdbc.queryForObject("SELECT id FROM fnd_dict_type WHERE code = 'event_level'", Long.class);
    for (Object[] row :
        new Object[][] {
          {"international", "国际", 1},
          {"national", "全国", 2},
          {"provincial", "省/市", 3},
          {"club", "俱乐部/业余", 4}
        }) {
      if (jdbc.queryForObject(
              "SELECT COUNT(1) FROM fnd_dict_item WHERE type_id = ? AND code = ?",
              Integer.class,
              typeId,
              row[0])
          == 0) {
        jdbc.update(
            "INSERT INTO fnd_dict_item (type_id, code, label, sort_order) VALUES (?,?,?,?)",
            typeId,
            row[0],
            row[1],
            row[2]);
      }
    }

    if (jdbc.queryForObject("SELECT COUNT(1) FROM fnd_user WHERE username = 'admin'", Integer.class) == 0) {
      jdbc.update("INSERT INTO fnd_user (username, password_hash, status) VALUES ('admin', ?, 'active')", hash);
      long userId = jdbc.queryForObject("SELECT id FROM fnd_user WHERE username = 'admin'", Long.class);
      jdbc.update("INSERT INTO fnd_user_role (user_id, role_id) VALUES (?, ?)", userId, roleId);
    }

    for (Object[] row :
        new Object[][] {
          {"banner", 1, 0},
          {"preview_matches", 1, 1},
          {"featured_players", 1, 2},
          {"featured_articles", 1, 3},
          {"ext_feed", 1, 4},
          {"highlight_athletes", 1, 5}
        }) {
      if (jdbc.queryForObject("SELECT COUNT(1) FROM cfg_home_block WHERE block_key = ?", Integer.class, row[0])
          == 0) {
        jdbc.update(
            "INSERT INTO cfg_home_block (block_key, enabled, sort_order, config_json) VALUES (?,?,?,null)",
            row[0],
            row[1],
            row[2]);
      }
    }
  }
}
