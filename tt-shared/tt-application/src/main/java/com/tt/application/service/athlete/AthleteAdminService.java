package com.tt.application.service.athlete;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tt.application.service.governance.AuditService;
import com.tt.common.exception.BadRequestException;
import com.tt.common.support.JdbcSupport;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
  private final ObjectMapper objectMapper;

  public AthleteAdminService(JdbcTemplate jdbc, AuditService audit, ObjectMapper objectMapper) {
    this.jdbc = jdbc;
    this.audit = audit;
    this.objectMapper = objectMapper;
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
        "INSERT INTO reg_athlete (name, name_en, gender, birth_date, birth_place, nationality, height_cm, dominant_hand, playing_style, association, profile_title, profile_summary, hero_image_url, social_url, current_world_rank, highest_world_rank, ranking_points, major_identity, source_urls, extra_profile_json, data_collected_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
        athleteArgs(b),
        kh);
  }

  public Map<String, Object> updateAthlete(long id, Map<String, Object> b) {
    jdbc.update(
        "UPDATE reg_athlete SET name=?, name_en=?, gender=?, birth_date=?, birth_place=?, nationality=?, height_cm=?, dominant_hand=?, playing_style=?, association=?, profile_title=?, profile_summary=?, hero_image_url=?, social_url=?, current_world_rank=?, highest_world_rank=?, ranking_points=?, major_identity=?, source_urls=?, extra_profile_json=?, data_collected_at=? WHERE id=?",
        appendId(athleteArgs(b), id));
    return jdbc.queryForList("SELECT * FROM reg_athlete WHERE id = ?", id).get(0);
  }

  @Transactional
  @SuppressWarnings("unchecked")
  public Map<String, Object> importAthleteData(Map<String, Object> body, long adminId) {
    if (body == null) throw new BadRequestException("导入内容不能为空");
    Map<String, Object> athlete =
        body.get("athlete") instanceof Map ? (Map<String, Object>) body.get("athlete") : body;
    String name = text(athlete.get("name"));
    if (name == null) throw new BadRequestException("athlete.name 必填");

    Map<String, Object> ranking =
        body.get("ranking") instanceof Map ? (Map<String, Object>) body.get("ranking") : Map.of();
    Map<String, Object> normalized = new LinkedHashMap<>(athlete);
    normalized.put("current_world_rank", first(ranking.get("current_world_rank"), athlete.get("current_world_rank")));
    normalized.put("highest_world_rank", first(ranking.get("highest_world_rank"), athlete.get("highest_world_rank")));
    normalized.put("ranking_points", first(ranking.get("ranking_points"), athlete.get("ranking_points")));
    normalized.put(
        "data_collected_at",
        first(body.get("data_collected_at"), ranking.get("ranking_date"), body.get("retrieved_at")));
    normalized.put("source_urls", sourceUrls(listOfMaps(body.get("sources"))));
    normalized.put("extra_profile_json", jsonOrNull(body.get("extra_profile")));

    Long id = targetAthleteId(athlete);
    String mode = "updated";
    if (id != null && exists(id)) {
      updateAthlete(id, normalized);
    } else {
      Long matched = findExistingAthlete(name, text(normalized.get("association")));
      if (matched != null) {
        id = matched;
        updateAthlete(id, normalized);
      } else {
        Map<String, Object> created = createAthlete(normalized);
        id = ((Number) created.get("id")).longValue();
        mode = "created";
      }
    }

    replaceStructuredData(id, body);
    jdbc.update(
        "INSERT INTO reg_athlete_import_snapshot (athlete_id, source_name, payload_json) VALUES (?,?,?)",
        id,
        text(body.get("source_name")) != null ? text(body.get("source_name")) : "quick_import",
        jsonOrNull(body) != null ? jsonOrNull(body) : String.valueOf(body));
    audit.write(adminId, "import", "athlete", id, name);

    Map<String, Object> row = jdbc.queryForList("SELECT * FROM reg_athlete WHERE id = ?", id).get(0);
    return Map.of(
        "ok",
        true,
        "mode",
        mode,
        "athlete",
        row,
        "counts",
        Map.of(
            "achievements", countList(body.get("achievements")),
            "results", countList(body.get("results")),
            "upcoming_events", countList(body.get("upcoming_events")),
            "sources", countList(body.get("sources"))));
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

  private Object[] athleteArgs(Map<String, Object> b) {
    return new Object[] {
      valueOrEmpty(b.get("name")),
      b.get("name_en"),
      b.get("gender"),
      JdbcSupport.parseDate(b.get("birth_date")),
      b.get("birth_place"),
      b.get("nationality"),
      intOrNull(b.get("height_cm")),
      b.get("dominant_hand"),
      b.get("playing_style"),
      b.get("association"),
      b.get("profile_title"),
      b.get("profile_summary"),
      b.get("hero_image_url"),
      b.get("social_url"),
      intOrNull(b.get("current_world_rank")),
      intOrNull(b.get("highest_world_rank")),
      intOrNull(b.get("ranking_points")),
      b.get("major_identity"),
      b.get("source_urls"),
      b.get("extra_profile_json"),
      JdbcSupport.parseTs(b.get("data_collected_at"))
    };
  }

  private Object[] appendId(Object[] args, long id) {
    Object[] out = new Object[args.length + 1];
    System.arraycopy(args, 0, out, 0, args.length);
    out[args.length] = id;
    return out;
  }

  private void replaceStructuredData(long athleteId, Map<String, Object> body) {
    jdbc.update("DELETE FROM reg_athlete_achievement WHERE athlete_id = ?", athleteId);
    jdbc.update("DELETE FROM reg_athlete_result WHERE athlete_id = ?", athleteId);
    jdbc.update("DELETE FROM reg_athlete_upcoming_event WHERE athlete_id = ?", athleteId);
    jdbc.update("DELETE FROM reg_athlete_data_source WHERE athlete_id = ?", athleteId);
    insertAchievements(athleteId, listOfMaps(body.get("achievements")));
    insertResults(athleteId, listOfMaps(body.get("results")));
    insertUpcomingEvents(athleteId, listOfMaps(body.get("upcoming_events")));
    insertSources(athleteId, listOfMaps(body.get("sources")));
  }

  private void insertAchievements(long athleteId, List<Map<String, Object>> rows) {
    int i = 0;
    for (Map<String, Object> row : rows) {
      jdbc.update(
          "INSERT INTO reg_athlete_achievement (athlete_id, year, event_name, category, result_label, partner_or_team, opponent, score, source_url, sort_order) VALUES (?,?,?,?,?,?,?,?,?,?)",
          athleteId,
          intOrNull(row.get("year")),
          valueOrEmpty(first(row.get("event_name"), row.get("event"))),
          row.get("category"),
          first(row.get("result_label"), row.get("result")),
          row.get("partner_or_team"),
          row.get("opponent"),
          row.get("score"),
          row.get("source_url"),
          intOrDefault(row.get("sort_order"), i++));
    }
  }

  private void insertResults(long athleteId, List<Map<String, Object>> rows) {
    int i = 0;
    for (Map<String, Object> row : rows) {
      jdbc.update(
          "INSERT INTO reg_athlete_result (athlete_id, match_date, event_name, category, result_label, partner_or_team, opponent, score, source_url, sort_order) VALUES (?,?,?,?,?,?,?,?,?,?)",
          athleteId,
          JdbcSupport.parseDate(first(row.get("match_date"), row.get("date"))),
          valueOrEmpty(first(row.get("event_name"), row.get("event"))),
          row.get("category"),
          first(row.get("result_label"), row.get("result")),
          row.get("partner_or_team"),
          row.get("opponent"),
          row.get("score"),
          row.get("source_url"),
          intOrDefault(row.get("sort_order"), i++));
    }
  }

  private void insertUpcomingEvents(long athleteId, List<Map<String, Object>> rows) {
    int i = 0;
    for (Map<String, Object> row : rows) {
      jdbc.update(
          "INSERT INTO reg_athlete_upcoming_event (athlete_id, start_date, end_date, event_name, level_label, location, venue, status, note, source_url, sort_order) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
          athleteId,
          JdbcSupport.parseDate(row.get("start_date")),
          JdbcSupport.parseDate(row.get("end_date")),
          valueOrEmpty(first(row.get("event_name"), row.get("event"))),
          first(row.get("level_label"), row.get("level")),
          row.get("location"),
          row.get("venue"),
          row.get("status"),
          first(row.get("note"), row.get("watch_note"), row.get("relevance_to_athlete")),
          row.get("source_url"),
          intOrDefault(row.get("sort_order"), i++));
    }
  }

  private void insertSources(long athleteId, List<Map<String, Object>> rows) {
    for (Map<String, Object> row : rows) {
      String url = text(row.get("url"));
      if (url == null) continue;
      jdbc.update(
          "INSERT INTO reg_athlete_data_source (athlete_id, source_name, source_url, retrieved_at, note) VALUES (?,?,?,?,?)",
          athleteId,
          text(row.get("name")) != null ? text(row.get("name")) : "source",
          url,
          JdbcSupport.parseTs(row.get("retrieved_at")),
          row.get("note"));
    }
  }

  private Long targetAthleteId(Map<String, Object> athlete) {
    Object raw = first(athlete.get("id"), athlete.get("athlete_id"));
    if (raw == null || "".equals(raw)) return null;
    if (raw instanceof Number) return ((Number) raw).longValue();
    try {
      return Long.parseLong(String.valueOf(raw));
    } catch (Exception e) {
      return null;
    }
  }

  private boolean exists(long id) {
    Integer total = jdbc.queryForObject("SELECT COUNT(1) FROM reg_athlete WHERE id = ?", Integer.class, id);
    return total != null && total > 0;
  }

  private Long findExistingAthlete(String name, String association) {
    List<Map<String, Object>> rows;
    if (association == null) {
      rows = jdbc.queryForList("SELECT id FROM reg_athlete WHERE name = ? ORDER BY id DESC LIMIT 1", name);
    } else {
      rows =
          jdbc.queryForList(
              "SELECT id FROM reg_athlete WHERE name = ? AND (association = ? OR association IS NULL OR association = '') ORDER BY id DESC LIMIT 1",
              name,
              association);
    }
    return rows.isEmpty() ? null : ((Number) rows.get(0).get("id")).longValue();
  }

  private String sourceUrls(List<Map<String, Object>> sources) {
    if (sources == null || sources.isEmpty()) return null;
    List<String> urls = new ArrayList<>();
    for (Map<String, Object> source : sources) {
      String url = text(source.get("url"));
      if (url != null) urls.add(url);
    }
    return urls.isEmpty() ? null : String.join("\n", urls);
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> listOfMaps(Object raw) {
    if (!(raw instanceof List)) return List.of();
    List<Map<String, Object>> out = new ArrayList<>();
    for (Object item : (List<Object>) raw) {
      if (item instanceof Map) out.add((Map<String, Object>) item);
    }
    return out;
  }

  private int countList(Object raw) {
    return raw instanceof List ? ((List<?>) raw).size() : 0;
  }

  private Object first(Object... values) {
    for (Object value : values) {
      if (value == null) continue;
      if (value instanceof String && ((String) value).trim().isEmpty()) continue;
      return value;
    }
    return null;
  }

  private String text(Object v) {
    if (v == null) return null;
    String s = String.valueOf(v).trim();
    return s.isEmpty() ? null : s;
  }

  private Object valueOrEmpty(Object v) {
    String s = text(v);
    return s == null ? "" : s;
  }

  private Integer intOrNull(Object v) {
    if (v == null || "".equals(v)) return null;
    if (v instanceof Number) return ((Number) v).intValue();
    try {
      return Integer.parseInt(String.valueOf(v));
    } catch (Exception e) {
      return null;
    }
  }

  private int intOrDefault(Object v, int d) {
    Integer n = intOrNull(v);
    return n == null ? d : n;
  }

  private String jsonOrNull(Object v) {
    if (v == null) return null;
    try {
      return objectMapper.writeValueAsString(v);
    } catch (JsonProcessingException e) {
      return null;
    }
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
