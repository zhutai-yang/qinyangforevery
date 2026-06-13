package com.tt.application.mq;

import com.tt.application.service.ext.ExtSyncService;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ExtSyncListener {

  private static final Logger log = LoggerFactory.getLogger(ExtSyncListener.class);

  private final ExtSyncService extSyncService;

  public ExtSyncListener(ExtSyncService extSyncService) {
    this.extSyncService = extSyncService;
  }

  @RabbitListener(queues = "${tt.mq.ext-sync-queue:tt.ext.sync}")
  public void onSyncMessage(Map<String, Object> payload) {
    if (payload == null) return;
    Object sid = payload.get("sourceId");
    long sourceId = sid instanceof Number n ? n.longValue() : Long.parseLong(String.valueOf(sid));
    try {
      extSyncService.runSyncSource(sourceId);
      log.info("Ext sync completed for sourceId={}", sourceId);
    } catch (Exception e) {
      log.error("Ext sync failed for sourceId=" + sourceId, e);
    }
  }
}
