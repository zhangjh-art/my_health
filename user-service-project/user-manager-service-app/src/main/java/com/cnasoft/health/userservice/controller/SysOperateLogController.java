package com.cnasoft.health.userservice.controller;

import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.userservice.model.SysOperateLog;
import com.cnasoft.health.userservice.service.ISysOperateLogService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import static com.cnasoft.health.common.vo.CommonResult.success;

@Slf4j
@RestController
@Api(tags = "系统操作日志API")
public class SysOperateLogController {
    @Resource
    private ISysOperateLogService sysOperateLogService;

    @GetMapping("/log/getLog")
    CommonResult<Object> getLogById(@RequestParam("logId") Long logId) {
        return success(sysOperateLogService.getById(logId));
    }

    @PostMapping("/log/addLog")
    CommonResult<Object> addLog() {
        sysOperateLogService.add();
        return success();
    }

    @GetMapping("/log/auth/getLog")
    CommonResult<SysOperateLog> getLogById_1(@RequestParam("logId") Long logId) {
        return success(sysOperateLogService.getById(logId));
    }

    @PostMapping("/log/auth/addLog")
    CommonResult<Object> addLog_1() {
        sysOperateLogService.add();
        return success();
    }
}
