package com.tt.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tt.infrastructure.persistence.entity.FndUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FndUserMapper extends BaseMapper<FndUser> {}
