package com.tt.application.service.ext;

import com.tt.common.support.JdbcSupport;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExtAdminService {

  private final JdbcTemplate jdbc;

  public ExtAdminService(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Map<String, Object> extSources() {
    return Map.of("list", jdbc.queryForList("SELECT * FROM ext_data_source ORDER BY id DESC"));
  }

  @Transactional
  public Map<String, Object> createExtSource(Map<String, Object> b) {
    GeneratedKeyHolder kh = new GeneratedKeyHolder();
    return JdbcSupport.insertAndSelect(
        jdbc,
        "ext_data_source",
        "INSERT INTO ext_data_source (name, base_url, parser_type, enabled, interval_minutes) VALUES (?,?,?,?,?)",
        new Object[] {
          b.getOrDefault("name", ""),
          b.getOrDefault("base_url", ""),
          b.getOrDefault("parser_type", "html_v1"),
          b.get("enabled") == null || Boolean.TRUE.equals(b.get("enabled")) ? 1 : 0,
          b.get("interval_minutes") != null ? ((Number) b.get("interval_minutes")).intValue() : 60
        },
        kh);
  }

  public Map<String, Object> updateExtSource(long id, Map<String, Object> b) {
    jdbc.update(
        "UPDATE ext_data_source SET name=?, base_url=?, parser_type=?, enabled=?, interval_minutes=? WHERE id=?",
        b.get("name"),
        b.get("base_url"),
        b.getOrDefault("parser_type", "html_v1"),
        Boolean.TRUE.equals(b.get("enabled")) ? 1 : 0,
        b.get("interval_minutes") != null ? ((Number) b.get("interval_minutes")).intValue() : 60,
        id);
    return jdbc.queryForList("SELECT * FROM ext_data_source WHERE id = ?", id).get(0);
  }

  public void deleteExtSource(long id) {
    jdbc.update("DELETE FROM ext_data_source WHERE id = ?", id);
  }

  public Map<String, Object> extSyncLogs(int page, int pageSize) {
    int size = JdbcSupport.pageSize(pageSize, 100);
    int off = (JdbcSupport.page(page) - 1) * size;
    int total = jdbc.queryForObject("SELECT COUNT(1) FROM ext_sync_log", Integer.class);
    List<Map<String, Object>> list =
        jdbc.queryForList(
            "SELECT l.*, s.name AS source_name FROM ext_sync_log l "
                + "LEFT JOIN ext_data_source s ON s.id = l.source_id "
                + "ORDER BY l.id DESC LIMIT ? OFFSET ?",
            size,
            off);
    return Map.of("list", list, "total", total, "page", JdbcSupport.page(page), "pageSize", size);
  }
}
