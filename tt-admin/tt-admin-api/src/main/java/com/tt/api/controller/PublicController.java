package com.tt.api.controller;

import com.tt.application.service.publicapi.PublicService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicController {

  private final PublicService publicService;

  public PublicController(PublicService publicService) {
    this.publicService = publicService;
  }

  @GetMapping("/events")
  public Map<String, Object> events(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int pageSize) {
    return publicService.listEvents(page, pageSize);
  }

  @GetMapping("/events/{id}")
  public Map<String, Object> event(@PathVariable long id) {
    return publicService.getEvent(id);
  }

  @GetMapping("/events/{id}/matches")
  public Map<String, Object> matches(
      @PathVariable long id,
      @RequestParam(required = false) Long stage_id,
      @RequestParam(required = false) String from,
      @RequestParam(required = false) String to) {
    return publicService.matchesForEvent(id, stage_id, from, to);
  }

  @GetMapping("/events/{id}/preview-matches")
  public Map<String, Object> preview(
      @PathVariable long id, @RequestParam(defaultValue = "7") int days) {
    return publicService.previewMatches(id, days);
  }

  @GetMapping("/matches/{id}")
  public Map<String, Object> match(@PathVariable long id) {
    return publicService.getMatch(id);
  }

  @GetMapping("/articles")
  public Map<String, Object> articles(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int pageSize) {
    return publicService.listArticles(page, pageSize);
  }

  @GetMapping("/articles/{slugOrId}")
  public Map<String, Object> article(@PathVariable String slugOrId) {
    return publicService.getArticle(slugOrId);
  }

  @GetMapping("/home/featured-articles")
  public Map<String, Object> featuredArticles(@RequestParam(defaultValue = "5") int limit) {
    return publicService.featuredArticles(limit);
  }

  @GetMapping("/home/featured-players")
  public Map<String, Object> featuredPlayers() {
    return publicService.featuredPlayers();
  }

  @GetMapping("/athletes/{id}")
  public Map<String, Object> athlete(@PathVariable long id) {
    return publicService.getAthlete(id);
  }

  @GetMapping("/home/layout")
  public Map<String, Object> layout() {
    return publicService.homeLayout();
  }

  @GetMapping("/ext-feed")
  public Map<String, Object> extFeed(
      @RequestParam(required = false) Integer source_id,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int pageSize) {
    return publicService.extFeed(source_id, page, pageSize);
  }
}
