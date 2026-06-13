package com.tt.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tt.infrastructure.persistence.entity.GovAuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GovAuditLogMapper extends BaseMapper<GovAuditLog> {}
