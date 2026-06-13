package com.tt.application.service.schedule;

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
public class ScheduleAdminService {

  private final JdbcTemplate jdbc;
  private final AuditService audit;

  public ScheduleAdminService(JdbcTemplate jdbc, AuditService audit) {
    this.jdbc = jdbc;
    this.audit = audit;
  }

  public Map<String, Object> listStages(long eventId) {
    return Map.of(
        "list",
        jdbc.queryForList(
            "SELECT * FROM sch_stage WHERE event_id = ? ORDER BY sort_order, id", eventId));
  }

  @Transactional
  public Map<String, Object> createStage(long eventId, Map<String, Object> b) {
    GeneratedKeyHolder kh = new GeneratedKeyHolder();
    return JdbcSupport.insertAndSelect(
        jdbc,
        "sch_stage",
        "INSERT INTO sch_stage (event_id, name, stage_type, sort_order) VALUES (?,?,?,?)",
        new Object[] {
          eventId,
          b.getOrDefault("name", ""),
          b.get("stage_type"),
          b.get("sort_order") != null ? ((Number) b.get("sort_order")).intValue() : 0
        },
        kh);
  }

  public Map<String, Object> updateStage(long id, Map<String, Object> b) {
    jdbc.update(
        "UPDATE sch_stage SET name=?, stage_type=?, sort_order=? WHERE id=?",
        b.get("name"),
        b.get("stage_type"),
        b.get("sort_order") != null ? ((Number) b.get("sort_order")).intValue() : 0,
        id);
    return jdbc.queryForList("SELECT * FROM sch_stage WHERE id = ?", id).get(0);
  }

  public void deleteStage(long id) {
    jdbc.update("DELETE FROM sch_stage WHERE id = ?", id);
  }

  public Map<String, Object> listMatchesAdmin(long eventId) {
    List<Map<String, Object>> matches =
        jdbc.queryForList(
            "SELECT * FROM `sch_match` WHERE event_id = ? ORDER BY scheduled_at, id", eventId);
    List<Long> ids = matches.stream().map(m -> ((Number) m.get("id")).longValue()).toList();
    Map<Long, List<Map<String, Object>>> parts = new LinkedHashMap<>();
    Map<Long, Map<String, Object>> resMap = new LinkedHashMap<>();
    if (!ids.isEmpty()) {
      String in = ids.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("");
      List<Map<String, Object>> p =
          jdbc.queryForList(
              "SELECT p.*, a.name AS athlete_name FROM `sch_match_participant` p "
                  + "INNER JOIN reg_athlete a ON a.id = p.athlete_id WHERE p.match_id IN ("
                  + in
                  + ") ORDER BY p.match_id, p.side_order");
      for (Map<String, Object> row : p) {
        long mid = ((Number) row.get("match_id")).longValue();
        parts.computeIfAbsent(mid, k -> new ArrayList<>()).add(row);
      }
      List<Map<String, Object>> rq =
          jdbc.queryForList("SELECT * FROM rec_result WHERE match_id IN (" + in + ")");
      for (Map<String, Object> r : rq) {
        resMap.put(((Number) r.get("match_id")).longValue(), r);
      }
    }
    List<Map<String, Object>> list = new ArrayList<>();
    for (Map<String, Object> m : matches) {
      long mid = ((Number) m.get("id")).longValue();
      Map<String, Object> item = new LinkedHashMap<>(m);
      item.put("participants", parts.getOrDefault(mid, List.of()));
      item.put("result", resMap.get(mid));
      list.add(item);
    }
    return Map.of("list", list);
  }

  @Transactional
  public Map<String, Object> createMatch(long eventId, Map<String, Object> b) {
    GeneratedKeyHolder kh = new GeneratedKeyHolder();
    return JdbcSupport.insertAndSelect(
        jdbc,
        "`sch_match`",
        "INSERT INTO `sch_match` (event_id, stage_id, scheduled_at, venue_id, round_label, table_no, status) VALUES (?,?,?,?,?,?,'scheduled')",
        new Object[] {
          eventId,
          ((Number) b.get("stage_id")).longValue(),
          JdbcSupport.parseTs(b.get("scheduled_at")),
          JdbcSupport.numOrNull(b.get("venue_id")),
          b.get("round_label"),
          b.get("table_no")
        },
        kh);
  }

  public Map<String, Object> updateMatch(long id, Map<String, Object> b) {
    jdbc.update(
        "UPDATE `sch_match` SET stage_id=?, scheduled_at=?, venue_id=?, round_label=?, table_no=?, status=? WHERE id=?",
        ((Number) b.get("stage_id")).longValue(),
        JdbcSupport.parseTs(b.get("scheduled_at")),
        JdbcSupport.numOrNull(b.get("venue_id")),
        b.get("round_label"),
        b.get("table_no"),
        b.getOrDefault("status", "scheduled"),
        id);
    return jdbc.queryForList("SELECT * FROM `sch_match` WHERE id = ?", id).get(0);
  }

  public void deleteMatch(long id) {
    jdbc.update("DELETE FROM `sch_match` WHERE id = ?", id);
  }

  @SuppressWarnings("unchecked")
  public void setParticipants(long matchId, Map<String, Object> body) {
    List<Number> aids = (List<Number>) body.getOrDefault("athleteIds", List.of());
    if (aids.size() != 2) throw new BadRequestException("单打需恰好两名选手");
    jdbc.update("DELETE FROM `sch_match_participant` WHERE match_id = ?", matchId);
    int o = 1;
    for (Number aid : aids) {
      jdbc.update(
          "INSERT INTO `sch_match_participant` (match_id, athlete_id, side_order) VALUES (?,?,?)",
          matchId,
          aid.longValue(),
          o++);
    }
  }

  public Map<String, Object> saveResult(long matchId, Map<String, Object> b, long adminId) {
    Integer ex =
        jdbc.queryForObject(
            "SELECT COUNT(1) FROM rec_result WHERE match_id = ?", Integer.class, matchId);
    int sh = JdbcSupport.parseInt(b.get("score_home"), 0);
    int sa = JdbcSupport.parseInt(b.get("score_away"), 0);
    Long wid = JdbcSupport.numOrNull(b.get("winner_athlete_id"));
    if (ex != null && ex > 0) {
      jdbc.update(
          "UPDATE rec_result SET winner_athlete_id=?, score_home=?, score_away=?, finished_at=CURRENT_TIMESTAMP(3) WHERE match_id=?",
          wid,
          sh,
          sa,
          matchId);
    } else {
      jdbc.update(
          "INSERT INTO rec_result (match_id, winner_athlete_id, score_home, score_away, finished_at) VALUES (?,?,?,?, CURRENT_TIMESTAMP(3))",
          matchId,
          wid,
          sh,
          sa);
    }
    jdbc.update("UPDATE `sch_match` SET status = 'finished' WHERE id = ?", matchId);
    audit.write(adminId, "update", "match_result", matchId, b.toString());
    return jdbc.queryForList("SELECT * FROM rec_result WHERE match_id = ?", matchId).get(0);
  }

  public Map<String, Object> rankingRebuild(long eventId) {
    List<Map<String, Object>> list =
        jdbc.queryForList(
            "SELECT p.athlete_id, COUNT(CASE WHEN r.winner_athlete_id = p.athlete_id THEN 1 END) AS wins, "
                + "COUNT(*) AS played FROM `sch_match_participant` p "
                + "INNER JOIN `sch_match` m ON m.id = p.match_id AND m.event_id = ? AND m.status = 'finished' "
                + "LEFT JOIN rec_result r ON r.match_id = m.id GROUP BY p.athlete_id ORDER BY wins DESC, played DESC",
            eventId);
    return Map.of("list", list);
  }
}
