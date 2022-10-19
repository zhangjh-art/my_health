package com.cnasoft.health.userservice.mapper;

import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.model.MQMessageConsumed;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MQMessageConsumedMapper extends SuperMapper<MQMessageConsumed> {
}
