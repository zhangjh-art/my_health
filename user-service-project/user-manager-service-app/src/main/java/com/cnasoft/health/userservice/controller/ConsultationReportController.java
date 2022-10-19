package com.cnasoft.health.userservice.controller;

import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.userservice.feign.dto.ConsultationReportReqVO;
import com.cnasoft.health.userservice.service.IConsultationReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author zjh
 * @date 2022/7/19
 */
@RestController
@Slf4j
@Api(tags = "咨询报告api")
public class ConsultationReportController {

    @Resource
    private IConsultationReportService consultationReportService;

    @PostMapping("/consultationReport/create")
    @ApiOperation("新建咨询报告")
    public CommonResult<Object> create(
        @RequestBody @Validated(ConsultationReportReqVO.Create.class) ConsultationReportReqVO vo) throws Exception {
        consultationReportService.createLocked(vo);
        return CommonResult.success();
    }

    @PutMapping("/consultationReport/update")
    @ApiOperation("编辑咨询报告")
    public CommonResult<Object> update(
        @RequestBody @Validated(ConsultationReportReqVO.Update.class) ConsultationReportReqVO vo) {
        consultationReportService.update(vo);
        return CommonResult.success();
    }

    @GetMapping("/consultationReport/get")
    @ApiOperation("根据预约id查询咨询报告详情")
    public CommonResult<Object> get(@RequestParam("reservationId") Long reservationId) {
        return CommonResult.success(consultationReportService.get(reservationId));
    }
}
