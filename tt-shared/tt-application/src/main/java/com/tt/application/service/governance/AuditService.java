package com.tt.application.service.governance;

import com.tt.infrastructure.persistence.entity.GovAuditLog;
import com.tt.infrastructure.persistence.mapper.GovAuditLogMapper;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

  private final GovAuditLogMapper govAuditLogMapper;

  public AuditService(GovAuditLogMapper govAuditLogMapper) {
    this.govAuditLogMapper = govAuditLogMapper;
  }

  public void write(Long userId, String action, String entity, Object entityId, String summary) {
    GovAuditLog log = new GovAuditLog();
    log.setUserId(userId);
    log.setAction(action);
    log.setEntity(entity);
    log.setEntityId(entityId == null ? null : String.valueOf(entityId));
    log.setPayloadSummary(summary);
    govAuditLogMapper.insert(log);
  }
}
