package com.cnasoft.health.userservice.service;

import com.cnasoft.health.common.service.ISuperService;
import com.cnasoft.health.userservice.model.SysOperateLog;

/**
 * 操作日志
 *
 * @author ganghe
 */
public interface ISysOperateLogService extends ISuperService<SysOperateLog> {
    SysOperateLog getById(Long id);
    void add();
}
