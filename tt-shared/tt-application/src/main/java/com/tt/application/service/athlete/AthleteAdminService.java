package com.tt.application.service.athlete;

import com.tt.application.service.governance.AuditService;
import com.tt.common.support.JdbcSupport;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AthleteAdminService {

  private final JdbcTemplate jdbc;
  private final AuditService audit;

  public AthleteAdminService(JdbcTemplate jdbc, AuditService audit) {
    this.jdbc = jdbc;
    this.audit = audit;
  }

  public Map<String, Object> listAthletes(int page, int pageSize) {
    int size = JdbcSupport.pageSize(pageSize, 200);
    int off = (JdbcSupport.page(page) - 1) * size;
    int total = jdbc.queryForObject("SELECT COUNT(1) FROM reg_athlete", Integer.class);
    List<Map<String, Object>> list =
        jdbc.queryForList(
            "SELECT * FROM reg_athlete ORDER BY id DESC LIMIT ? OFFSET ?", size, off);
    return Map.of("list", list, "total", total, "page", JdbcSupport.page(page), "pageSize", size);
  }

  @Transactional
  public Map<String, Object> createAthlete(Map<String, Object> b) {
    GeneratedKeyHolder kh = new GeneratedKeyHolder();
    return JdbcSupport.insertAndSelect(
        jdbc,
        "reg_athlete",
        "INSERT INTO reg_athlete (name, gender, birth_date, association) VALUES (?,?,?,?)",
        new Object[] {
          b.getOrDefault("name", ""),
          b.get("gender"),
          JdbcSupport.parseDate(b.get("birth_date")),
          b.get("association")
        },
        kh);
  }

  public Map<String, Object> updateAthlete(long id, Map<String, Object> b) {
    jdbc.update(
        "UPDATE reg_athlete SET name=?, gender=?, birth_date=?, association=? WHERE id=?",
        b.get("name"),
        b.get("gender"),
        JdbcSupport.parseDate(b.get("birth_date")),
        b.get("association"),
        id);
    return jdbc.queryForList("SELECT * FROM reg_athlete WHERE id = ?", id).get(0);
  }

  public void deleteAthlete(long id) {
    jdbc.update("DELETE FROM reg_athlete WHERE id = ?", id);
  }

  public Map<String, Object> getAthleteHighlights(long athleteId) {
    List<Map<String, Object>> list =
        jdbc.queryForList(
            "SELECT id, athlete_id, title, cover_url, summary, sort_order, status, published_at, created_at, updated_at "
                + "FROM cfg_athlete_highlight WHERE athlete_id = ? ORDER BY sort_order, id",
            athleteId);
    return Map.of("list", list);
  }

  @Transactional
  public void putAthleteHighlights(long athleteId, List<Map<String, Object>> body, long adminId) {
    jdbc.update("DELETE FROM cfg_athlete_highlight WHERE athlete_id = ?", athleteId);
    if (body == null) body = List.of();
    for (Map<String, Object> row : body) {
      jdbc.update(
          "INSERT INTO cfg_athlete_highlight (athlete_id, title, cover_url, summary, sort_order, status, published_at) "
              + "VALUES (?,?,?,?,?,?,?)",
          athleteId,
          row.get("title") != null ? row.get("title").toString() : "",
          row.get("cover_url") != null ? row.get("cover_url").toString() : null,
          row.get("summary") != null ? row.get("summary").toString() : null,
          row.get("sort_order") != null ? ((Number) row.get("sort_order")).intValue() : 0,
          row.get("status") != null ? row.get("status").toString() : "draft",
          JdbcSupport.parseTs(row.get("published_at")));
    }
    audit.write(
        adminId, "update", "athlete_highlights", athleteId, body != null ? body.toString() : "[]");
  }

  public Map<String, Object> getAthleteBusinessPreviews(long athleteId) {
    List<Map<String, Object>> list =
        jdbc.queryForList(
            "SELECT id, athlete_id, title, cover_url, summary, link_url, link_text, scheduled_at, sort_order, status, published_at, created_at, updated_at "
                + "FROM cfg_athlete_business_preview WHERE athlete_id = ? ORDER BY sort_order, id",
            athleteId);
    return Map.of("list", list);
  }

  @Transactional
  public void putAthleteBusinessPreviews(
      long athleteId, List<Map<String, Object>> body, long adminId) {
    jdbc.update("DELETE FROM cfg_athlete_business_preview WHERE athlete_id = ?", athleteId);
    if (body == null) body = List.of();
    for (Map<String, Object> row : body) {
      jdbc.update(
          "INSERT INTO cfg_athlete_business_preview (athlete_id, title, cover_url, summary, link_url, link_text, scheduled_at, sort_order, status, published_at) "
              + "VALUES (?,?,?,?,?,?,?,?,?,?)",
          athleteId,
          row.get("title") != null ? row.get("title").toString() : "",
          row.get("cover_url") != null ? row.get("cover_url").toString() : null,
          row.get("summary") != null ? row.get("summary").toString() : null,
          row.get("link_url") != null ? row.get("link_url").toString() : null,
          row.get("link_text") != null ? row.get("link_text").toString() : null,
          JdbcSupport.parseTs(row.get("scheduled_at")),
          row.get("sort_order") != null ? ((Number) row.get("sort_order")).intValue() : 0,
          row.get("status") != null ? row.get("status").toString() : "draft",
          JdbcSupport.parseTs(row.get("published_at")));
    }
    audit.write(
        adminId,
        "update",
        "athlete_business_previews",
        athleteId,
        body != null ? body.toString() : "[]");
  }
}
