package com.tt.application.service.cms;

import com.tt.common.support.JdbcSupport;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CmsAdminService {

  private final JdbcTemplate jdbc;

  public CmsAdminService(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Map<String, Object> listArticlesAdmin(int page, int pageSize) {
    int size = JdbcSupport.pageSize(pageSize, 100);
    int off = (JdbcSupport.page(page) - 1) * size;
    int total = jdbc.queryForObject("SELECT COUNT(1) FROM cms_article", Integer.class);
    List<Map<String, Object>> list =
        jdbc.queryForList(
            "SELECT * FROM cms_article ORDER BY id DESC LIMIT ? OFFSET ?", size, off);
    return Map.of("list", list, "total", total, "page", JdbcSupport.page(page), "pageSize", size);
  }

  @Transactional
  public Map<String, Object> createArticle(Map<String, Object> b) {
    GeneratedKeyHolder kh = new GeneratedKeyHolder();
    return JdbcSupport.insertAndSelect(
        jdbc,
        "cms_article",
        "INSERT INTO cms_article (title, slug, summary, body, cover_url, status, published_at, pinned, event_id, athlete_id) VALUES (?,?,?,?,?,?,?,?,?,?)",
        new Object[] {
          b.getOrDefault("title", ""),
          b.get("slug"),
          b.get("summary"),
          b.get("body"),
          b.get("cover_url"),
          b.getOrDefault("status", "draft"),
          JdbcSupport.parseTs(b.get("published_at")),
          Boolean.TRUE.equals(b.get("pinned")) ? 1 : 0,
          JdbcSupport.numOrNull(b.get("event_id")),
          JdbcSupport.numOrNull(b.get("athlete_id"))
        },
        kh);
  }

  public Map<String, Object> updateArticle(long id, Map<String, Object> b) {
    jdbc.update(
        "UPDATE cms_article SET title=?, slug=?, summary=?, body=?, cover_url=?, status=?, published_at=?, pinned=?, event_id=?, athlete_id=?, updated_at=CURRENT_TIMESTAMP(3) WHERE id=?",
        b.get("title"),
        b.get("slug"),
        b.get("summary"),
        b.get("body"),
        b.get("cover_url"),
        b.getOrDefault("status", "draft"),
        JdbcSupport.parseTs(b.get("published_at")),
        Boolean.TRUE.equals(b.get("pinned")) ? 1 : 0,
        JdbcSupport.numOrNull(b.get("event_id")),
        JdbcSupport.numOrNull(b.get("athlete_id")),
        id);
    return jdbc.queryForList("SELECT * FROM cms_article WHERE id = ?", id).get(0);
  }

  public void deleteArticle(long id) {
    jdbc.update("DELETE FROM cms_article WHERE id = ?", id);
  }

  public Map<String, Object> featuredList() {
    return Map.of(
        "list",
        jdbc.queryForList(
            "SELECT f.*, a.name AS athlete_name FROM cfg_featured_athlete f "
                + "INNER JOIN reg_athlete a ON a.id = f.athlete_id ORDER BY f.sort_order, f.athlete_id"));
  }

  public void featuredPut(List<Map<String, Object>> list) {
    jdbc.update("DELETE FROM cfg_featured_athlete");
    for (Map<String, Object> row : list) {
      if (row.get("athlete_id") == null) continue;
      jdbc.update(
          "INSERT INTO cfg_featured_athlete (athlete_id, sort_order, enabled) VALUES (?,?,?)",
          ((Number) row.get("athlete_id")).longValue(),
          row.get("sort_order") != null ? ((Number) row.get("sort_order")).intValue() : 0,
          row.get("enabled") == null || Boolean.TRUE.equals(row.get("enabled")) ? 1 : 0);
    }
  }

  public Map<String, Object> homeConfigGet() {
    return Map.of(
        "blocks", jdbc.queryForList("SELECT * FROM cfg_home_block ORDER BY sort_order"));
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> homeConfigPut(Map<String, Object> body) {
    List<Map<String, Object>> blocks =
        (List<Map<String, Object>>) body.getOrDefault("blocks", List.of());
    for (Map<String, Object> b : blocks) {
      jdbc.update(
          "UPDATE cfg_home_block SET enabled=?, sort_order=?, config_json=? WHERE block_key=?",
          Boolean.TRUE.equals(b.get("enabled")) ? 1 : 0,
          b.get("sort_order") != null ? ((Number) b.get("sort_order")).intValue() : 0,
          b.get("config_json"),
          b.get("block_key"));
    }
    return homeConfigGet();
  }
}
