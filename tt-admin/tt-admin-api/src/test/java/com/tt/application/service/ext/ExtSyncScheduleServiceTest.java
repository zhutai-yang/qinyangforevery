package com.tt.application.service.ext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ExtSyncScheduleServiceTest {

  @Mock private JdbcTemplate jdbc;

  private ExtSyncScheduleService service;

  @BeforeEach
  void setUp() {
    service = new ExtSyncScheduleService(jdbc);
    ReflectionTestUtils.setField(service, "staleRunningMinutes", 30);
  }

  @Test
  void recoverStaleRunningJobs_updatesWithConfiguredMinutes() {
    when(jdbc.update(any(String.class), eq(30))).thenReturn(2);
    assertEquals(2, service.recoverStaleRunningJobs());
    verify(jdbc).update(any(String.class), eq(30));
  }

  @Test
  @SuppressWarnings("unchecked")
  void findDueSourceIds_queriesEnabledSources() {
    when(jdbc.query(any(String.class), any(RowMapper.class))).thenReturn(List.of(1L, 3L));
    assertEquals(List.of(1L, 3L), service.findDueSourceIds());
    verify(jdbc).query(any(String.class), any(RowMapper.class));
  }
}
