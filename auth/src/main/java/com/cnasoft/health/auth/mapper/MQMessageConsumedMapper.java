package com.cnasoft.health.auth.mapper;

import com.cnasoft.health.auth.model.MQMessageConsumed;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MQMessageConsumedMapper extends SuperMapper<MQMessageConsumed> {
}
