package com.tt.application.service.ext;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ExtSyncScheduleService {

  private static final String FIND_DUE_SOURCES_SQL =
      """
      SELECT s.id
      FROM ext_data_source s
      WHERE s.enabled = 1
        AND NOT EXISTS (
          SELECT 1 FROM ext_sync_log l
          WHERE l.source_id = s.id AND l.status = 'running'
        )
        AND (
          NOT EXISTS (SELECT 1 FROM ext_sync_log l WHERE l.source_id = s.id)
          OR (
            SELECT MAX(COALESCE(l.ended_at, l.started_at))
            FROM ext_sync_log l
            WHERE l.source_id = s.id
          ) <= DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL s.interval_minutes MINUTE)
        )
      ORDER BY s.id
      """;

  private static final String RECOVER_STALE_RUNNING_SQL =
      """
      UPDATE ext_sync_log
      SET ended_at = CURRENT_TIMESTAMP(3),
          status = 'error',
          error_message = 'stale running job recovered by scheduler'
      WHERE status = 'running'
        AND started_at < DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL ? MINUTE)
      """;

  private final JdbcTemplate jdbc;

  @Value("${tt.sync.scheduler.stale-running-minutes:30}")
  private int staleRunningMinutes;

  public ExtSyncScheduleService(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public int recoverStaleRunningJobs() {
    return jdbc.update(RECOVER_STALE_RUNNING_SQL, staleRunningMinutes);
  }

  public List<Long> findDueSourceIds() {
    return jdbc.query(FIND_DUE_SOURCES_SQL, (rs, rowNum) -> rs.getLong("id"));
  }
}
