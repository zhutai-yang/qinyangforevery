package com.tt.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tt.api.controller.PublicController;
import com.tt.application.service.publicapi.PublicService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PublicControllerTest {

  private MockMvc mockMvc;
  private StubPublicService publicService;

  @BeforeEach
  void setUp() {
    publicService = new StubPublicService();
    mockMvc = MockMvcBuilders.standaloneSetup(new PublicController(publicService)).build();
  }

  @Test
  void events_returnsPagedList() throws Exception {
    mockMvc
        .perform(get("/api/public/events").param("page", "1").param("pageSize", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.page").value(1))
        .andExpect(jsonPath("$.pageSize").value(20))
        .andExpect(jsonPath("$.total").value(0))
        .andExpect(jsonPath("$.list").isArray());
  }

  @Test
  void events_usesDefaultPageParams() throws Exception {
    mockMvc.perform(get("/api/public/events")).andExpect(status().isOk());
  }

  @Test
  void athleteDetailIncludesStarProfileAndShowcaseContent() throws Exception {
    mockMvc
        .perform(get("/api/public/athletes/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.athlete.id").value(1))
        .andExpect(jsonPath("$.athlete.name").exists())
        .andExpect(jsonPath("$.athlete.profile_title").exists())
        .andExpect(jsonPath("$.athlete.profile_summary").exists())
        .andExpect(jsonPath("$.upcoming").isArray())
        .andExpect(jsonPath("$.history").isArray())
        .andExpect(jsonPath("$.highlights").isArray())
        .andExpect(jsonPath("$.business_previews").isArray());
  }

  @Test
  void homeLayoutIncludesPremiumShowcaseBlocks() throws Exception {
    mockMvc
        .perform(get("/api/public/home/layout"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.blocks").isArray())
        .andExpect(jsonPath("$.highlight_athletes").isArray())
        .andExpect(jsonPath("$.highlight_athletes[0].athlete_name").exists())
        .andExpect(jsonPath("$.highlight_athletes[0].profile_title").exists());
  }

  private static class StubPublicService extends PublicService {

    StubPublicService() {
      super(null);
    }

    @Override
    public Map<String, Object> listEvents(int page, int pageSize) {
      return Map.of("list", List.of(), "total", 0, "page", page, "pageSize", pageSize);
    }

    @Override
    public Map<String, Object> getAthlete(long id) {
      return Map.of(
          "athlete",
          Map.of(
              "id",
              id,
              "name",
              "张三",
              "profile_title",
              "国乒新生代核心",
              "profile_summary",
              "前三板速度突出"),
          "upcoming",
          List.of(),
          "history",
          List.of(),
          "highlights",
          List.of(),
          "business_previews",
          List.of());
    }

    @Override
    public Map<String, Object> homeLayout() {
      return Map.of(
          "blocks",
          List.of(),
          "highlight_athletes",
          List.of(
              Map.of(
                  "athlete_id",
                  1,
                  "athlete_name",
                  "张三",
                  "profile_title",
                  "国乒新生代核心")));
    }
  }
}
