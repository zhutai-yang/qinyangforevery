package com.tt.common.support;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;

/** JDBC 公共辅助，供 application 层各领域服务复用。 */
public final class JdbcSupport {

  private JdbcSupport() {}

  public static int page(int page) {
    return Math.max(1, page);
  }

  public static int pageSize(int pageSize, int max) {
    return Math.min(max, Math.max(1, pageSize > 0 ? pageSize : 20));
  }

  public static Map<String, Object> insertAndSelect(
      JdbcTemplate jdbc, String table, String insertSql, Object[] args, GeneratedKeyHolder kh) {
    jdbc.update(
        conn -> {
          PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
          for (int i = 0; i < args.length; i++) {
            ps.setObject(i + 1, args[i]);
          }
          return ps;
        },
        kh);
    long id = kh.getKey().longValue();
    return jdbc.queryForList("SELECT * FROM " + table + " WHERE id = ?", id).get(0);
  }

  public static Date parseDate(Object v) {
    if (v == null) return null;
    if (v instanceof Date) return (Date) v;
    String s = String.valueOf(v);
    if (s.isEmpty()) return null;
    return Date.valueOf(s.length() > 10 ? s.substring(0, 10) : s);
  }

  public static Timestamp parseTs(Object v) {
    if (v == null) return null;
    if (v instanceof Timestamp) return (Timestamp) v;
    String s = String.valueOf(v).replace('T', ' ');
    if (s.isEmpty()) return null;
    try {
      return Timestamp.valueOf(s.length() <= 10 ? s + " 00:00:00" : s);
    } catch (Exception e) {
      return null;
    }
  }

  public static Long numOrNull(Object v) {
    if (v == null || "".equals(v)) return null;
    return ((Number) v).longValue();
  }

  public static int parseInt(Object v, int d) {
    if (v == null) return d;
    try {
      return Integer.parseInt(String.valueOf(v));
    } catch (Exception e) {
      return d;
    }
  }
}
