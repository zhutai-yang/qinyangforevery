package com.tt.application.service.publicapi;

import com.tt.common.exception.NotFoundException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class PublicService {

  private final JdbcTemplate jdbc;

  public PublicService(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  private static int page(int p) {
    return Math.max(1, p);
  }

  private static int pageSize(int ps, int max) {
    return Math.min(max, Math.max(1, ps > 0 ? ps : 20));
  }

  public Map<String, Object> listEvents(int page, int pageSize) {
    int ps = pageSize(pageSize, 100);
    int off = (page(page) - 1) * ps;
    Integer total =
        jdbc.queryForObject(
            "SELECT COUNT(1) FROM evt_event WHERE status = 'published'", Integer.class);
    List<Map<String, Object>> list =
        jdbc.queryForList(
            "SELECT id, name, edition, level_code, start_date, end_date, location, status, updated_at "
                + "FROM evt_event WHERE status = 'published' "
                + "ORDER BY start_date DESC, id DESC LIMIT ? OFFSET ?",
            ps,
            off);
    return Map.of("list", list, "total", total, "page", page(page), "pageSize", ps);
  }

  public Map<String, Object> getEvent(long id) {
    List<Map<String, Object>> ev =
        jdbc.queryForList(
            "SELECT e.* FROM evt_event e WHERE e.id = ? AND e.status = 'published'", id);
    if (ev.isEmpty()) throw new NotFoundException("赛事不存在或未发布");
    Map<String, Object> row = new LinkedHashMap<>(ev.get(0));
    List<Map<String, Object>> venues =
        jdbc.queryForList(
            "SELECT v.id, v.name, v.address FROM evt_venue v "
                + "INNER JOIN evt_event_venue ev ON ev.venue_id = v.id WHERE ev.event_id = ?",
            id);
    row.put("venues", venues);
    return row;
  }

  public Map<String, Object> matchesForEvent(long eventId, Long stageId, String from, String to) {
    StringBuilder where = new StringBuilder("m.event_id = ?");
    List<Object> args = new ArrayList<>();
    args.add(eventId);
    if (stageId != null) {
      where.append(" AND m.stage_id = ?");
      args.add(stageId);
    }
    if (from != null && !from.isEmpty()) {
      where.append(" AND m.scheduled_at >= ?");
      args.add(Timestamp.valueOf(from.replace(" ", "T").length() <= 10 ? from + "T00:00:00" : from));
    }
    if (to != null && !to.isEmpty()) {
      where.append(" AND m.scheduled_at <= ?");
      args.add(Timestamp.valueOf(to.replace(" ", "T").length() <= 10 ? to + "T23:59:59" : to));
    }
    String sql =
        "SELECT m.id, m.event_id, m.stage_id, m.scheduled_at, m.venue_id, m.round_label, m.table_no, m.status, "
            + "s.name AS stage_name, v.name AS venue_name FROM `sch_match` m "
            + "INNER JOIN sch_stage s ON s.id = m.stage_id "
            + "LEFT JOIN evt_venue v ON v.id = m.venue_id WHERE "
            + where
            + " ORDER BY m.scheduled_at, m.id";
    List<Map<String, Object>> matches = jdbc.queryForList(sql, args.toArray());
    List<Long> ids = matches.stream().map(m -> ((Number) m.get("id")).longValue()).toList();
    Map<Long, List<Map<String, Object>>> byMatch = new HashMap<>();
    Map<Long, Map<String, Object>> resBy = new HashMap<>();
    if (!ids.isEmpty()) {
      String in = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
      List<Map<String, Object>> parts =
          jdbc.queryForList(
              "SELECT p.match_id, p.athlete_id, p.side_order, a.name AS athlete_name "
                  + "FROM `sch_match_participant` p INNER JOIN reg_athlete a ON a.id = p.athlete_id "
                  + "WHERE p.match_id IN ("
                  + in
                  + ") ORDER BY p.side_order");
      for (Map<String, Object> p : parts) {
        long mid = ((Number) p.get("match_id")).longValue();
        byMatch.computeIfAbsent(mid, k -> new ArrayList<>()).add(p);
      }
      List<Map<String, Object>> results =
          jdbc.queryForList(
              "SELECT match_id, winner_athlete_id, score_home, score_away FROM rec_result WHERE match_id IN ("
                  + in
                  + ")");
      for (Map<String, Object> r : results) {
        resBy.put(((Number) r.get("match_id")).longValue(), r);
      }
    }
    List<Map<String, Object>> list = new ArrayList<>();
    for (Map<String, Object> m : matches) {
      long mid = ((Number) m.get("id")).longValue();
      Map<String, Object> item = new LinkedHashMap<>(m);
      item.put("participants", byMatch.getOrDefault(mid, List.of()));
      item.put("result", resBy.get(mid));
      list.add(item);
    }
    return Map.of("list", list, "total", list.size());
  }

  public Map<String, Object> previewMatches(long eventId, int days) {
    int d = Math.min(30, Math.max(1, days));
    long until = System.currentTimeMillis() + d * 86400000L;
    List<Map<String, Object>> rows =
        jdbc.queryForList(
            "SELECT m.id, m.scheduled_at, m.round_label, m.stage_id, s.name AS stage_name "
                + "FROM `sch_match` m INNER JOIN sch_stage s ON s.id = m.stage_id "
                + "INNER JOIN evt_event e ON e.id = m.event_id "
                + "WHERE m.event_id = ? AND e.status = 'published' AND m.status = 'scheduled' "
                + "AND m.scheduled_at IS NOT NULL AND m.scheduled_at <= ? ORDER BY m.scheduled_at",
            eventId,
            new Timestamp(until));
    List<Long> ids = rows.stream().map(r -> ((Number) r.get("id")).longValue()).toList();
    Map<Long, List<Map<String, Object>>> byM = new HashMap<>();
    if (!ids.isEmpty()) {
      String in = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
      List<Map<String, Object>> parts =
          jdbc.queryForList(
              "SELECT p.match_id, p.athlete_id, p.side_order, a.name AS athlete_name "
                  + "FROM `sch_match_participant` p INNER JOIN reg_athlete a ON a.id = p.athlete_id "
                  + "WHERE p.match_id IN ("
                  + in
                  + ") ORDER BY p.side_order");
      for (Map<String, Object> p : parts) {
        long mid = ((Number) p.get("match_id")).longValue();
        byM.computeIfAbsent(mid, k -> new ArrayList<>()).add(p);
      }
    }
    List<Map<String, Object>> list = new ArrayList<>();
    for (Map<String, Object> m : rows) {
      long mid = ((Number) m.get("id")).longValue();
      Map<String, Object> item = new LinkedHashMap<>(m);
      item.put("participants", byM.getOrDefault(mid, List.of()));
      list.add(item);
    }
    return Map.of("list", list);
  }

  public Map<String, Object> getMatch(long id) {
    List<Map<String, Object>> rows =
        jdbc.queryForList(
            "SELECT m.*, s.name AS stage_name, e.status AS event_status FROM `sch_match` m "
                + "INNER JOIN sch_stage s ON s.id = m.stage_id "
                + "INNER JOIN evt_event e ON e.id = m.event_id "
                + "WHERE m.id = ? AND e.status = 'published'",
            id);
    if (rows.isEmpty()) throw new NotFoundException("场次不存在");
    Map<String, Object> m = new LinkedHashMap<>(rows.get(0));
    List<Map<String, Object>> parts =
        jdbc.queryForList(
            "SELECT p.athlete_id, p.side_order, a.name AS athlete_name FROM `sch_match_participant` p "
                + "INNER JOIN reg_athlete a ON a.id = p.athlete_id WHERE p.match_id = ? ORDER BY p.side_order",
            id);
    List<Map<String, Object>> res =
        jdbc.queryForList("SELECT * FROM rec_result WHERE match_id = ?", id);
    m.put("participants", parts);
    m.put("result", res.isEmpty() ? null : res.get(0));
    return m;
  }

  public Map<String, Object> listArticles(int page, int pageSize) {
    int ps = pageSize(pageSize, 100);
    int off = (page(page) - 1) * ps;
    Integer total =
        jdbc.queryForObject(
            "SELECT COUNT(1) FROM cms_article WHERE status = 'published'", Integer.class);
    List<Map<String, Object>> list =
        jdbc.queryForList(
            "SELECT id, title, slug, summary, cover_url, published_at, pinned, event_id, athlete_id "
                + "FROM cms_article WHERE status = 'published' "
                + "ORDER BY pinned DESC, published_at DESC LIMIT ? OFFSET ?",
            ps,
            off);
    return Map.of("list", list, "total", total, "page", page(page), "pageSize", ps);
  }

  public Map<String, Object> getArticle(String slugOrId) {
    List<Map<String, Object>> rows;
    if (slugOrId.matches("^\\d+$")) {
      rows =
          jdbc.queryForList(
              "SELECT * FROM cms_article WHERE id = ? AND status = 'published'",
              Long.parseLong(slugOrId));
    } else {
      rows =
          jdbc.queryForList(
              "SELECT * FROM cms_article WHERE slug = ? AND status = 'published'", slugOrId);
    }
    if (rows.isEmpty()) throw new NotFoundException("文章不存在");
    return rows.get(0);
  }

  public Map<String, Object> featuredArticles(int limit) {
    int lim = Math.min(20, Math.max(1, limit > 0 ? limit : 5));
    List<Map<String, Object>> list =
        jdbc.queryForList(
            "SELECT id, title, slug, summary, cover_url, published_at FROM cms_article "
                + "WHERE status = 'published' ORDER BY pinned DESC, published_at DESC LIMIT ?",
            lim);
    return Map.of("list", list);
  }

  public Map<String, Object> featuredPlayers() {
    return Map.of("list", buildFeaturedPlayersList());
  }

  private List<Map<String, Object>> buildFeaturedPlayersList() {
    List<Map<String, Object>> fa =
        jdbc.queryForList(
            "SELECT athlete_id, sort_order FROM cfg_featured_athlete WHERE enabled = 1 "
                + "ORDER BY sort_order, athlete_id");
    List<Map<String, Object>> out = new ArrayList<>();
    for (Map<String, Object> row : fa) {
      long aid = ((Number) row.get("athlete_id")).longValue();
      List<Map<String, Object>> ath =
          jdbc.queryForList(
              "SELECT id, name, gender, association FROM reg_athlete WHERE id = ?", aid);
      if (ath.isEmpty()) continue;
      List<Map<String, Object>> nextM =
          jdbc.queryForList(
              "SELECT m.id, m.scheduled_at, m.round_label, e.name AS event_name "
                  + "FROM `sch_match_participant` p INNER JOIN `sch_match` m ON m.id = p.match_id "
                  + "INNER JOIN evt_event e ON e.id = m.event_id AND e.status = 'published' "
                  + "WHERE p.athlete_id = ? AND m.status = 'scheduled' AND m.scheduled_at >= CURRENT_TIMESTAMP(3) "
                  + "ORDER BY m.scheduled_at LIMIT 1",
              aid);
      Map<String, Object> item = new LinkedHashMap<>();
      item.put("athlete", ath.get(0));
      item.put("next_match", nextM.isEmpty() ? null : nextM.get(0));
      out.add(item);
    }
    return out;
  }

  public Map<String, Object> getAthlete(long id) {
    List<Map<String, Object>> a =
        jdbc.queryForList("SELECT * FROM reg_athlete WHERE id = ?", id);
    if (a.isEmpty()) throw new NotFoundException("球员不存在");
    List<Map<String, Object>> matches =
        jdbc.queryForList(
            "SELECT m.id, m.scheduled_at, m.status, m.round_label, e.name AS event_name, e.id AS event_id "
                + "FROM `sch_match_participant` p INNER JOIN `sch_match` m ON m.id = p.match_id "
                + "INNER JOIN evt_event e ON e.id = m.event_id AND e.status = 'published' "
                + "WHERE p.athlete_id = ? ORDER BY m.scheduled_at DESC",
            id);
    List<Long> mids =
        matches.stream().map(m -> ((Number) m.get("id")).longValue()).toList();
    Map<Long, Map<String, Object>> resMap = new HashMap<>();
    if (!mids.isEmpty()) {
      String in = mids.stream().map(String::valueOf).collect(Collectors.joining(","));
      List<Map<String, Object>> rq =
          jdbc.queryForList(
              "SELECT r.match_id, r.winner_athlete_id, r.score_home, r.score_away FROM rec_result r "
                  + "WHERE r.match_id IN ("
                  + in
                  + ")");
      for (Map<String, Object> r : rq) {
        resMap.put(((Number) r.get("match_id")).longValue(), r);
      }
    }

    // 每个场次的参与球员（用于官网组合成 A vs B）
    Map<Long, List<Map<String, Object>>> participantsByMatch = new HashMap<>();
    if (!mids.isEmpty()) {
      String in = mids.stream().map(String::valueOf).collect(Collectors.joining(","));
      List<Map<String, Object>> parts =
          jdbc.queryForList(
              "SELECT p.match_id, p.athlete_id, p.side_order, a.name AS athlete_name "
                  + "FROM `sch_match_participant` p INNER JOIN reg_athlete a ON a.id = p.athlete_id "
                  + "WHERE p.match_id IN ("
                  + in
                  + ") ORDER BY p.match_id, p.side_order");
      for (Map<String, Object> p : parts) {
        long mid = ((Number) p.get("match_id")).longValue();
        participantsByMatch
            .computeIfAbsent(mid, k -> new ArrayList<>())
            .add(
                Map.of(
                    "athlete_id", p.get("athlete_id"),
                    "side_order", p.get("side_order"),
                    "athlete_name", p.get("athlete_name")));
      }
    }

    List<Map<String, Object>> upcoming = new ArrayList<>();
    List<Map<String, Object>> history = new ArrayList<>();
    for (Map<String, Object> m : matches) {
      long mid = ((Number) m.get("id")).longValue();
      Map<String, Object> item = new LinkedHashMap<>(m);
      Map<String, Object> res = resMap.get(mid);
      item.put("result", res);
      item.put("participants", participantsByMatch.getOrDefault(mid, List.of()));
      if ("scheduled".equals(m.get("status")) || res == null) upcoming.add(item);
      else history.add(item);
    }

    List<Map<String, Object>> highlights =
        jdbc.queryForList(
            "SELECT id, athlete_id, title, cover_url, summary, sort_order, status, published_at "
                + "FROM cfg_athlete_highlight WHERE athlete_id = ? AND status = 'published' "
                + "ORDER BY sort_order, id",
            id);

    List<Map<String, Object>> businessPreviews =
        jdbc.queryForList(
            "SELECT id, athlete_id, title, cover_url, summary, link_url, link_text, scheduled_at, sort_order, status, published_at "
                + "FROM cfg_athlete_business_preview "
                + "WHERE athlete_id = ? AND status = 'published' ORDER BY sort_order, id",
            id);

    return Map.of(
        "athlete",
        a.get(0),
        "upcoming",
        upcoming,
        "history",
        history,
        "highlights",
        highlights,
        "business_previews",
        businessPreviews);
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> homeLayout() {
    List<Map<String, Object>> blocks =
        jdbc.queryForList(
            "SELECT block_key, enabled, sort_order, config_json FROM cfg_home_block ORDER BY sort_order");
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("blocks", blocks);
    for (Map<String, Object> b : blocks) {
      boolean en = Boolean.TRUE.equals(b.get("enabled")) || Integer.valueOf(1).equals(b.get("enabled"));
      if (!en) continue;
      String key = (String) b.get("block_key");
      if ("featured_articles".equals(key)) {
        List<Map<String, Object>> r =
            jdbc.queryForList(
                "SELECT id, title, slug, summary, cover_url, published_at FROM cms_article "
                    + "WHERE status = 'published' ORDER BY pinned DESC, published_at DESC LIMIT 5");
        data.put("featured_articles", r);
      } else if ("featured_players".equals(key)) {
        data.put("featured_players", buildFeaturedPlayersList());
      } else if ("highlight_athletes".equals(key)) {
        // 高光运动员：从 cfg_athlete_highlight 里拿已发布内容，并按 athlete_id 分组取每人一条高光
        // 这里不依赖 featured_players 配置，直接展示所有已发布高光内容中的前 N 位运动员。
        int limit = 5;
        List<Map<String, Object>> highlights =
            jdbc.queryForList(
                "SELECT h.id, h.athlete_id, h.title, h.cover_url, h.summary, h.published_at, "
                    + "a.name AS athlete_name "
                    + "FROM cfg_athlete_highlight h "
                    + "INNER JOIN reg_athlete a ON a.id = h.athlete_id "
                    + "WHERE h.status = 'published' "
                    + "ORDER BY h.sort_order, h.id "
                    + "LIMIT 200");
        Map<Long, Map<String, Object>> firstByAthlete = new LinkedHashMap<>();
        for (Map<String, Object> h : highlights) {
          long aid = ((Number) h.get("athlete_id")).longValue();
          if (firstByAthlete.containsKey(aid)) continue;
          Map<String, Object> item = new LinkedHashMap<>();
          item.put("athlete_id", aid);
          item.put("athlete_name", h.get("athlete_name"));
          item.put("title", h.get("title"));
          item.put("cover_url", h.get("cover_url"));
          item.put("summary", h.get("summary"));
          item.put("published_at", h.get("published_at"));
          firstByAthlete.put(aid, item);
          if (firstByAthlete.size() >= limit) break;
        }
        data.put("highlight_athletes", new ArrayList<>(firstByAthlete.values()));
      } else if ("preview_matches".equals(key)) {
        List<Map<String, Object>> r =
            jdbc.queryForList(
                "SELECT m.id, m.scheduled_at, m.round_label, e.name AS event_name, e.id AS event_id "
                    + "FROM `sch_match` m INNER JOIN evt_event e ON e.id = m.event_id AND e.status = 'published' "
                    + "WHERE m.status = 'scheduled' AND m.scheduled_at >= CURRENT_TIMESTAMP(3) ORDER BY m.scheduled_at LIMIT 8");
        List<Long> ids = r.stream().map(x -> ((Number) x.get("id")).longValue()).toList();
        Map<Long, List<String>> byM = new HashMap<>();
        if (!ids.isEmpty()) {
          String in = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
          List<Map<String, Object>> parts =
              jdbc.queryForList(
                  "SELECT p.match_id, a.name AS athlete_name, p.side_order FROM `sch_match_participant` p "
                      + "INNER JOIN reg_athlete a ON a.id = p.athlete_id WHERE p.match_id IN ("
                      + in
                      + ") ORDER BY p.match_id, p.side_order");
          for (Map<String, Object> p : parts) {
            long mid = ((Number) p.get("match_id")).longValue();
            byM.computeIfAbsent(mid, k -> new ArrayList<>()).add((String) p.get("athlete_name"));
          }
        }
        List<Map<String, Object>> pm = new ArrayList<>();
        for (Map<String, Object> m : r) {
          long mid = ((Number) m.get("id")).longValue();
          Map<String, Object> row = new LinkedHashMap<>(m);
          row.put("players", String.join(" vs ", byM.getOrDefault(mid, List.of())));
          pm.add(row);
        }
        data.put("preview_matches", pm);
      } else if ("ext_feed".equals(key)) {
        try {
          List<Map<String, Object>> r =
              jdbc.queryForList(
                  "SELECT i.id, i.title, i.external_url, i.summary, i.published_at_ext, i.created_at, s.name AS source_name "
                      + "FROM ext_feed_item i INNER JOIN ext_data_source s ON s.id = i.source_id ORDER BY i.created_at DESC LIMIT 6");
          data.put("ext_feed", Map.of("list", r, "degraded", false));
        } catch (Exception e) {
          data.put("ext_feed", Map.of("list", List.of(), "degraded", true));
        }
      }
    }
    return data;
  }

  public Map<String, Object> extFeed(Integer sourceId, int page, int pageSize) {
    int ps = pageSize(pageSize, 100);
    int off = (page(page) - 1) * ps;
    try {
      String where = "1=1";
      List<Object> countArgs = new ArrayList<>();
      List<Object> listArgs = new ArrayList<>();
      if (sourceId != null) {
        where += " AND i.source_id = ?";
        countArgs.add(sourceId.longValue());
        listArgs.add(sourceId.longValue());
      }
      listArgs.add(ps);
      listArgs.add(off);
      Integer total =
          jdbc.queryForObject(
              "SELECT COUNT(1) FROM ext_feed_item i WHERE " + where,
              Integer.class,
              countArgs.toArray());
      List<Map<String, Object>> list =
          jdbc.queryForList(
              "SELECT i.id, i.title, i.external_url, i.summary, i.published_at_ext, i.created_at, s.name AS source_name "
                  + "FROM ext_feed_item i INNER JOIN ext_data_source s ON s.id = i.source_id WHERE "
                  + where
                  + " ORDER BY i.created_at DESC LIMIT ? OFFSET ?",
              listArgs.toArray());
      return Map.of(
          "list", list, "total", total, "page", page(page), "pageSize", ps, "degraded", false);
    } catch (Exception e) {
      return Map.of(
          "list",
          List.of(),
          "total",
          0,
          "page",
          page(page),
          "pageSize",
          ps,
          "degraded",
          true);
    }
  }
}
