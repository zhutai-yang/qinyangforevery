package com.tt.application.service.ext;

import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ExtSyncDispatcher {

  private final ExtSyncService extSyncService;
  private final RabbitTemplate rabbitTemplate;

  @Value("${tt.sync.mode:direct}")
  private String syncMode;

  @Value("${tt.mq.ext-sync-queue:tt.ext.sync}")
  private String extSyncQueueName;

  public ExtSyncDispatcher(ExtSyncService extSyncService, RabbitTemplate rabbitTemplate) {
    this.extSyncService = extSyncService;
    this.rabbitTemplate = rabbitTemplate;
  }

  public Map<String, Object> triggerSync(long sourceId) {
    if ("queue".equals(syncMode)) {
      rabbitTemplate.convertAndSend("", extSyncQueueName, Map.of("sourceId", sourceId));
      return Map.of(
          "ok",
          true,
          "queued",
          true,
          "sourceId",
          sourceId,
          "message",
          "同步任务已入队，请稍后查看同步日志");
    }
    try {
      Map<String, Object> result = new HashMap<>(extSyncService.runSyncSource(sourceId));
      result.put("ok", true);
      result.put("queued", false);
      return result;
    } catch (Exception e) {
      throw new ExtSyncTriggerException(
          e.getMessage() != null ? e.getMessage() : "sync failed", e);
    }
  }

  public static class ExtSyncTriggerException extends RuntimeException {
    public ExtSyncTriggerException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
