package com.tt.application.service.event;

import com.tt.application.service.governance.AuditService;
import com.tt.common.exception.NotFoundException;
import com.tt.common.support.JdbcSupport;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventAdminService {

  private final JdbcTemplate jdbc;
  private final AuditService audit;

  public EventAdminService(JdbcTemplate jdbc, AuditService audit) {
    this.jdbc = jdbc;
    this.audit = audit;
  }

  public Map<String, Object> listEvents(int page, int pageSize, String status) {
    int size = JdbcSupport.pageSize(pageSize, 100);
    int off = (JdbcSupport.page(page) - 1) * size;
    int total;
    List<Map<String, Object>> list;
    if (status != null && !status.isEmpty()) {
      total =
          jdbc.queryForObject(
              "SELECT COUNT(1) FROM evt_event WHERE status = ?", Integer.class, status);
      list =
          jdbc.queryForList(
              "SELECT * FROM evt_event WHERE status = ? ORDER BY id DESC LIMIT ? OFFSET ?",
              status,
              size,
              off);
    } else {
      total = jdbc.queryForObject("SELECT COUNT(1) FROM evt_event", Integer.class);
      list =
          jdbc.queryForList(
              "SELECT * FROM evt_event ORDER BY id DESC LIMIT ? OFFSET ?", size, off);
    }
    return Map.of("list", list, "total", total, "page", JdbcSupport.page(page), "pageSize", size);
  }

  @Transactional
  public Map<String, Object> createEvent(Map<String, Object> b, long adminId) {
    GeneratedKeyHolder kh = new GeneratedKeyHolder();
    Map<String, Object> row =
        JdbcSupport.insertAndSelect(
            jdbc,
            "evt_event",
            "INSERT INTO evt_event (name, edition, level_code, start_date, end_date, location, status, description) VALUES (?,?,?,?,?,?,?,?)",
            new Object[] {
              b.getOrDefault("name", ""),
              b.get("edition"),
              b.get("level_code"),
              JdbcSupport.parseDate(b.get("start_date")),
              JdbcSupport.parseDate(b.get("end_date")),
              b.get("location"),
              b.getOrDefault("status", "draft"),
              b.get("description")
            },
            kh);
    audit.write(adminId, "create", "event", row.get("id"), String.valueOf(b.get("name")));
    return row;
  }

  public Map<String, Object> getEvent(long id) {
    List<Map<String, Object>> ev = jdbc.queryForList("SELECT * FROM evt_event WHERE id = ?", id);
    if (ev.isEmpty()) throw new NotFoundException("不存在");
    Map<String, Object> row = new LinkedHashMap<>(ev.get(0));
    row.put(
        "venues",
        jdbc.queryForList(
            "SELECT v.* FROM evt_venue v INNER JOIN evt_event_venue ev ON ev.venue_id = v.id WHERE ev.event_id = ?",
            id));
    return row;
  }

  public Map<String, Object> updateEvent(long id, Map<String, Object> b, long adminId) {
    jdbc.update(
        "UPDATE evt_event SET name=?, edition=?, level_code=?, start_date=?, end_date=?, location=?, status=?, description=?, updated_at=CURRENT_TIMESTAMP(3) WHERE id=?",
        b.get("name"),
        b.get("edition"),
        b.get("level_code"),
        JdbcSupport.parseDate(b.get("start_date")),
        JdbcSupport.parseDate(b.get("end_date")),
        b.get("location"),
        b.getOrDefault("status", "draft"),
        b.get("description"),
        id);
    audit.write(adminId, "update", "event", id, String.valueOf(b.get("name")));
    return jdbc.queryForList("SELECT * FROM evt_event WHERE id = ?", id).get(0);
  }

  public void deleteEvent(long id, long adminId) {
    jdbc.update("DELETE FROM evt_event WHERE id = ?", id);
    audit.write(adminId, "delete", "event", id, null);
  }

  @SuppressWarnings("unchecked")
  public void linkVenues(long eventId, Map<String, Object> body, long adminId) {
    List<Number> venueIds = (List<Number>) body.getOrDefault("venueIds", List.of());
    jdbc.update("DELETE FROM evt_event_venue WHERE event_id = ?", eventId);
    for (Number vid : venueIds) {
      jdbc.update(
          "INSERT INTO evt_event_venue (event_id, venue_id) VALUES (?, ?)", eventId, vid.longValue());
    }
    audit.write(
        adminId,
        "update",
        "event_venues",
        eventId,
        body.get("venueIds") != null ? body.get("venueIds").toString() : "[]");
  }

  public Map<String, Object> listVenues() {
    List<Map<String, Object>> list = jdbc.queryForList("SELECT * FROM evt_venue ORDER BY id DESC");
    return Map.of("list", list, "total", list.size());
  }

  @Transactional
  public Map<String, Object> createVenue(Map<String, Object> b, long adminId) {
    GeneratedKeyHolder kh = new GeneratedKeyHolder();
    Map<String, Object> row =
        JdbcSupport.insertAndSelect(
            jdbc,
            "evt_venue",
            "INSERT INTO evt_venue (name, address) VALUES (?,?)",
            new Object[] {b.getOrDefault("name", ""), b.get("address")},
            kh);
    audit.write(adminId, "create", "venue", row.get("id"), String.valueOf(b.get("name")));
    return row;
  }

  public void updateVenue(long id, Map<String, Object> b) {
    jdbc.update(
        "UPDATE evt_venue SET name=?, address=? WHERE id=?", b.get("name"), b.get("address"), id);
  }

  public void deleteVenue(long id) {
    jdbc.update("DELETE FROM evt_venue WHERE id = ?", id);
  }
}
