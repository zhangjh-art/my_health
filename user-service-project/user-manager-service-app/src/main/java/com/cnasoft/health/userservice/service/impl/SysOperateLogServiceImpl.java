package com.cnasoft.health.userservice.service.impl;

import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.userservice.mapper.SysOperateLogMapper;
import com.cnasoft.health.userservice.model.SysOperateLog;
import com.cnasoft.health.userservice.service.ISysOperateLogService;
import org.springframework.stereotype.Service;
import java.util.Date;

/**
 * 操作日志
 *
 * @author ganghe
 */
@Service
public class SysOperateLogServiceImpl extends SuperServiceImpl<SysOperateLogMapper, SysOperateLog> implements ISysOperateLogService {

    @Override
    public SysOperateLog getById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public void add() {
        SysOperateLog log = new SysOperateLog();
        log.setCommandId("1000");
        log.setCommandType("UPDATE");
        log.setCommandModel("SysOperateLog");
        log.setCommandSql("SELECT su.id, AES_DECRYPT(UNHEX(su.username), 's2MTjOFGZ8aibt81lHTXgnVKCAA6H5JxkbGEAm763jw=')\n" +
                "FROM sys_user su limit 10");
        log.setCreateBy(1l);
        log.setCreateTime(new Date());
        log.setUpdateBy(1l);
        log.setUpdateTime(new Date());
        log.setIsDeleted(false);
        baseMapper.insert(log);
    }
}
