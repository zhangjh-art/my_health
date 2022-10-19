package com.cnasoft.health.userservice.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.model.SysOperateLog;
import org.springframework.stereotype.Component;

/**
 * @author ganghe
 */
@DS(Constant.DATA_SOURCE_MYSQL)
@Component
public interface SysOperateLogMapper extends SuperMapper<SysOperateLog> {
}
