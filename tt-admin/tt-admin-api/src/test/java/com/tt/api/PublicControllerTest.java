package com.tt.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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
  private PublicService publicService;

  @BeforeEach
  void setUp() {
    publicService = mock(PublicService.class);
    mockMvc = MockMvcBuilders.standaloneSetup(new PublicController(publicService)).build();
  }

  @Test
  void events_returnsPagedList() throws Exception {
    when(publicService.listEvents(eq(1), eq(20)))
        .thenReturn(Map.of("list", List.of(), "total", 0, "page", 1, "pageSize", 20));
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
    when(publicService.listEvents(eq(1), eq(20)))
        .thenReturn(Map.of("list", List.of(), "total", 0, "page", 1, "pageSize", 20));
    mockMvc.perform(get("/api/public/events")).andExpect(status().isOk());
  }
}
