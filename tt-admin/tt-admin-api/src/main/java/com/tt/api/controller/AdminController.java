package com.tt.api.controller;

import com.tt.api.web.AdminUserId;
import com.tt.application.service.athlete.AthleteAdminService;
import com.tt.application.service.cms.CmsAdminService;
import com.tt.application.service.event.EventAdminService;
import com.tt.application.service.ext.ExtAdminService;
import com.tt.application.service.ext.ExtSyncDispatcher;
import com.tt.application.service.governance.GovernanceAdminService;
import com.tt.application.service.schedule.ScheduleAdminService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

  private final EventAdminService eventAdminService;
  private final ScheduleAdminService scheduleAdminService;
  private final AthleteAdminService athleteAdminService;
  private final CmsAdminService cmsAdminService;
  private final GovernanceAdminService governanceAdminService;
  private final ExtAdminService extAdminService;
  private final ExtSyncDispatcher extSyncDispatcher;

  public AdminController(
      EventAdminService eventAdminService,
      ScheduleAdminService scheduleAdminService,
      AthleteAdminService athleteAdminService,
      CmsAdminService cmsAdminService,
      GovernanceAdminService governanceAdminService,
      ExtAdminService extAdminService,
      ExtSyncDispatcher extSyncDispatcher) {
    this.eventAdminService = eventAdminService;
    this.scheduleAdminService = scheduleAdminService;
    this.athleteAdminService = athleteAdminService;
    this.cmsAdminService = cmsAdminService;
    this.governanceAdminService = governanceAdminService;
    this.extAdminService = extAdminService;
    this.extSyncDispatcher = extSyncDispatcher;
  }

  private long uid() {
    return AdminUserId.get();
  }

  @GetMapping("/events")
  public Map<String, Object> events(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int pageSize,
      @RequestParam(required = false) String status) {
    return eventAdminService.listEvents(page, pageSize, status);
  }

  @PostMapping("/events")
  public ResponseEntity<Map<String, Object>> createEvent(@RequestBody Map<String, Object> body) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(eventAdminService.createEvent(body, uid()));
  }

  @GetMapping("/events/{id}")
  public Map<String, Object> getEvent(@PathVariable long id) {
    return eventAdminService.getEvent(id);
  }

  @PutMapping("/events/{id}")
  public Map<String, Object> updateEvent(
      @PathVariable long id, @RequestBody Map<String, Object> body) {
    return eventAdminService.updateEvent(id, body, uid());
  }

  @DeleteMapping("/events/{id}")
  public Map<String, Object> deleteEvent(@PathVariable long id) {
    eventAdminService.deleteEvent(id, uid());
    return Map.of("ok", true);
  }

  @PutMapping("/events/{id}/venues")
  public Map<String, Object> linkVenues(
      @PathVariable long id, @RequestBody Map<String, Object> body) {
    eventAdminService.linkVenues(id, body, uid());
    return Map.of("ok", true);
  }

  @GetMapping("/venues")
  public Map<String, Object> venues() {
    return eventAdminService.listVenues();
  }

  @PostMapping("/venues")
  public ResponseEntity<Map<String, Object>> createVenue(@RequestBody Map<String, Object> body) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(eventAdminService.createVenue(body, uid()));
  }

  @PutMapping("/venues/{id}")
  public Map<String, Object> updateVenue(@PathVariable long id, @RequestBody Map<String, Object> body) {
    eventAdminService.updateVenue(id, body);
    return Map.of("ok", true);
  }

  @DeleteMapping("/venues/{id}")
  public Map<String, Object> deleteVenue(@PathVariable long id) {
    eventAdminService.deleteVenue(id);
    return Map.of("ok", true);
  }

  @GetMapping("/events/{eventId}/stages")
  public Map<String, Object> stages(@PathVariable long eventId) {
    return scheduleAdminService.listStages(eventId);
  }

  @PostMapping("/events/{eventId}/stages")
  public ResponseEntity<Map<String, Object>> createStage(
      @PathVariable long eventId, @RequestBody Map<String, Object> body) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(scheduleAdminService.createStage(eventId, body));
  }

  @PutMapping("/stages/{id}")
  public Map<String, Object> updateStage(@PathVariable long id, @RequestBody Map<String, Object> body) {
    return scheduleAdminService.updateStage(id, body);
  }

  @DeleteMapping("/stages/{id}")
  public Map<String, Object> deleteStage(@PathVariable long id) {
    scheduleAdminService.deleteStage(id);
    return Map.of("ok", true);
  }

  @GetMapping("/events/{eventId}/matches")
  public Map<String, Object> matches(@PathVariable long eventId) {
    return scheduleAdminService.listMatchesAdmin(eventId);
  }

  @PostMapping("/events/{eventId}/matches")
  public ResponseEntity<Map<String, Object>> createMatch(
      @PathVariable long eventId, @RequestBody Map<String, Object> body) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(scheduleAdminService.createMatch(eventId, body));
  }

  @PutMapping("/matches/{id}")
  public Map<String, Object> updateMatch(@PathVariable long id, @RequestBody Map<String, Object> body) {
    return scheduleAdminService.updateMatch(id, body);
  }

  @DeleteMapping("/matches/{id}")
  public Map<String, Object> deleteMatch(@PathVariable long id) {
    scheduleAdminService.deleteMatch(id);
    return Map.of("ok", true);
  }

  @PutMapping("/matches/{matchId}/participants")
  public Map<String, Object> participants(
      @PathVariable long matchId, @RequestBody Map<String, Object> body) {
    scheduleAdminService.setParticipants(matchId, body);
    return Map.of("ok", true);
  }

  @PutMapping("/matches/{matchId}/result")
  public Map<String, Object> result(
      @PathVariable long matchId, @RequestBody Map<String, Object> body) {
    return scheduleAdminService.saveResult(matchId, body, uid());
  }

  @PostMapping("/events/{eventId}/ranking/rebuild")
  public Map<String, Object> ranking(@PathVariable long eventId) {
    return scheduleAdminService.rankingRebuild(eventId);
  }

  @GetMapping("/athletes")
  public Map<String, Object> athletes(
      @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int pageSize) {
    return athleteAdminService.listAthletes(page, pageSize);
  }

  @PostMapping("/athletes")
  public ResponseEntity<Map<String, Object>> createAthlete(@RequestBody Map<String, Object> body) {
    return ResponseEntity.status(HttpStatus.CREATED).body(athleteAdminService.createAthlete(body));
  }

  @PutMapping("/athletes/{id}")
  public Map<String, Object> updateAthlete(@PathVariable long id, @RequestBody Map<String, Object> body) {
    return athleteAdminService.updateAthlete(id, body);
  }

  @DeleteMapping("/athletes/{id}")
  public Map<String, Object> deleteAthlete(@PathVariable long id) {
    athleteAdminService.deleteAthlete(id);
    return Map.of("ok", true);
  }

  @GetMapping("/athletes/{athleteId}/highlights")
  public Map<String, Object> athleteHighlights(@PathVariable long athleteId) {
    return athleteAdminService.getAthleteHighlights(athleteId);
  }

  @PutMapping("/athletes/{athleteId}/highlights")
  public Map<String, Object> athleteHighlightsPut(
      @PathVariable long athleteId, @RequestBody List<Map<String, Object>> body) {
    athleteAdminService.putAthleteHighlights(athleteId, body, uid());
    return Map.of("ok", true);
  }

  @GetMapping("/athletes/{athleteId}/business-previews")
  public Map<String, Object> athleteBusinessPreviews(@PathVariable long athleteId) {
    return athleteAdminService.getAthleteBusinessPreviews(athleteId);
  }

  @PutMapping("/athletes/{athleteId}/business-previews")
  public Map<String, Object> athleteBusinessPreviewsPut(
      @PathVariable long athleteId, @RequestBody List<Map<String, Object>> body) {
    athleteAdminService.putAthleteBusinessPreviews(athleteId, body, uid());
    return Map.of("ok", true);
  }

  @GetMapping("/dict/{typeCode}")
  public Map<String, Object> dictGet(@PathVariable String typeCode) {
    return governanceAdminService.getDict(typeCode);
  }

  @PutMapping("/dict/{typeCode}")
  public Map<String, Object> dictPut(@PathVariable String typeCode, @RequestBody Map<String, Object> body) {
    governanceAdminService.putDict(typeCode, body);
    return Map.of("ok", true);
  }

  @GetMapping("/audit-logs")
  public Map<String, Object> audit(
      @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int pageSize) {
    return governanceAdminService.auditLogs(page, pageSize);
  }

  @GetMapping("/users")
  public Map<String, Object> users(
      @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int pageSize) {
    return governanceAdminService.listUsers(page, pageSize);
  }

  @PostMapping("/users")
  public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, Object> body) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(governanceAdminService.createUser(body, uid()));
  }

  @GetMapping("/users/{id}")
  public Map<String, Object> getUser(@PathVariable long id) {
    return governanceAdminService.getUserById(id);
  }

  @PutMapping("/users/{id}")
  public Map<String, Object> updateUser(
      @PathVariable long id, @RequestBody Map<String, Object> body) {
    return governanceAdminService.updateUser(id, body, uid());
  }

  @PutMapping("/users/{id}/password")
  public Map<String, Object> updateUserPassword(
      @PathVariable long id, @RequestBody Map<String, Object> body) {
    return governanceAdminService.updateUserPassword(id, body, uid());
  }

  @GetMapping("/articles")
  public Map<String, Object> articles(
      @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int pageSize) {
    return cmsAdminService.listArticlesAdmin(page, pageSize);
  }

  @PostMapping("/articles")
  public ResponseEntity<Map<String, Object>> createArticle(@RequestBody Map<String, Object> body) {
    return ResponseEntity.status(HttpStatus.CREATED).body(cmsAdminService.createArticle(body));
  }

  @PutMapping("/articles/{id}")
  public Map<String, Object> updateArticle(@PathVariable long id, @RequestBody Map<String, Object> body) {
    return cmsAdminService.updateArticle(id, body);
  }

  @DeleteMapping("/articles/{id}")
  public Map<String, Object> deleteArticle(@PathVariable long id) {
    cmsAdminService.deleteArticle(id);
    return Map.of("ok", true);
  }

  @GetMapping("/featured-athletes")
  public Map<String, Object> featuredGet() {
    return cmsAdminService.featuredList();
  }

  @PutMapping("/featured-athletes")
  public Map<String, Object> featuredPut(@RequestBody List<Map<String, Object>> body) {
    cmsAdminService.featuredPut(body);
    return Map.of("ok", true);
  }

  @GetMapping("/home-config")
  public Map<String, Object> homeGet() {
    return cmsAdminService.homeConfigGet();
  }

  @PutMapping("/home-config")
  public Map<String, Object> homePut(@RequestBody Map<String, Object> body) {
    return cmsAdminService.homeConfigPut(body);
  }

  @GetMapping("/ext-sources")
  public Map<String, Object> extList() {
    return extAdminService.extSources();
  }

  @PostMapping("/ext-sources")
  public ResponseEntity<Map<String, Object>> extCreate(@RequestBody Map<String, Object> body) {
    return ResponseEntity.status(HttpStatus.CREATED).body(extAdminService.createExtSource(body));
  }

  @PutMapping("/ext-sources/{id}")
  public Map<String, Object> extUpdate(@PathVariable long id, @RequestBody Map<String, Object> body) {
    return extAdminService.updateExtSource(id, body);
  }

  @DeleteMapping("/ext-sources/{id}")
  public Map<String, Object> extDelete(@PathVariable long id) {
    extAdminService.deleteExtSource(id);
    return Map.of("ok", true);
  }

  @GetMapping("/ext-sync-logs")
  public Map<String, Object> extLogs(
      @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int pageSize) {
    return extAdminService.extSyncLogs(page, pageSize);
  }

  @PostMapping("/ext-sources/{id}/sync")
  public ResponseEntity<?> extSync(@PathVariable long id) {
    try {
      Map<String, Object> body = extSyncDispatcher.triggerSync(id);
      if (Boolean.TRUE.equals(body.get("queued"))) {
        return ResponseEntity.accepted().body(body);
      }
      return ResponseEntity.ok(body);
    } catch (ExtSyncDispatcher.ExtSyncTriggerException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              Map.of(
                  "code",
                  "SYNC_FAILED",
                  "message",
                  e.getMessage() != null ? e.getMessage() : "error"));
    }
  }
}
