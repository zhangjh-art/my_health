package com.cnasoft.health.userservice.controller;

import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.userservice.feign.dto.ConsultationRecordReqVO;
import com.cnasoft.health.userservice.service.IConsultationRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author zjh
 * @date 2022/7/27
 */
@RestController
@Slf4j
@Api(tags = "咨询api")
public class ConsultationRecordController {

    @Resource
    private IConsultationRecordService consultationRecordService;

    @PostMapping("/consultation/create")
    @ApiOperation("新增咨询记录")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "source", value = "0:求助小精灵 1:在线咨询", required = true, dataType = "Integer", paramType = "query"),
        @ApiImplicitParam(name = "consultTypes", value = "咨询类型", dataType = "String", paramType = "query")})
    public CommonResult<Object> create(@RequestBody ConsultationRecordReqVO vo) {
        consultationRecordService.create(vo);
        return CommonResult.success();
    }

    @GetMapping("/consultation/list")
    @ApiOperation("根据用户id查询咨询记录列表")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageNum", value = "页码", required = true, dataType = "Integer", paramType = "query"),
        @ApiImplicitParam(name = "pageSize", value = "每页条数", required = true, dataType = "Integer", paramType = "query")})
    public CommonResult<Object> get(@RequestParam Map<String, Object> params) {
        return CommonResult.success(consultationRecordService.listByUserId(params));
    }
}
