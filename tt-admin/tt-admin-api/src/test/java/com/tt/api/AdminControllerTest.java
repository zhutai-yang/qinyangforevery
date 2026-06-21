package com.tt.api;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tt.api.controller.AdminController;
import com.tt.application.service.athlete.AthleteAdminService;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AdminControllerTest {

  private AthleteAdminService athleteAdminService;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    athleteAdminService = mock(AthleteAdminService.class);
    mockMvc =
        MockMvcBuilders.standaloneSetup(
                new AdminController(null, null, athleteAdminService, null, null, null, null))
            .build();
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(7L, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void importAthlete_routesPayloadToServiceWithAdminUser() throws Exception {
    when(athleteAdminService.importAthleteData(anyMap(), eq(7L)))
        .thenReturn(
            Map.of(
                "ok",
                true,
                "mode",
                "created",
                "athlete",
                Map.of("id", 12, "name", "王楚钦")));

    mockMvc
        .perform(
            post("/api/admin/athletes/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"athlete\":{\"name\":\"王楚钦\"}}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.mode").value("created"))
        .andExpect(jsonPath("$.athlete.id").value(12));

    verify(athleteAdminService).importAthleteData(anyMap(), eq(7L));
  }
}
