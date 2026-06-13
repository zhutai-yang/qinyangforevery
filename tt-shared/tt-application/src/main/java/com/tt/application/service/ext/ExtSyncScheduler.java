package com.tt.application.service.ext;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "tt.sync.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class ExtSyncScheduler {

  private static final Logger log = LoggerFactory.getLogger(ExtSyncScheduler.class);

  private final ExtSyncScheduleService scheduleService;
  private final ExtSyncDispatcher dispatcher;

  public ExtSyncScheduler(ExtSyncScheduleService scheduleService, ExtSyncDispatcher dispatcher) {
    this.scheduleService = scheduleService;
    this.dispatcher = dispatcher;
  }

  @Scheduled(fixedDelayString = "${tt.sync.scheduler.fixed-delay-ms:60000}")
  public void runDueSyncs() {
    int recovered = scheduleService.recoverStaleRunningJobs();
    if (recovered > 0) {
      log.warn("Recovered {} stale ext sync log(s)", recovered);
    }
    List<Long> due = scheduleService.findDueSourceIds();
    if (due.isEmpty()) {
      return;
    }
    log.info("Triggering scheduled ext sync for {} source(s)", due.size());
    for (Long sourceId : due) {
      try {
        dispatcher.triggerSync(sourceId);
      } catch (Exception e) {
        log.error("Scheduled ext sync trigger failed for sourceId={}", sourceId, e);
      }
    }
  }
}
