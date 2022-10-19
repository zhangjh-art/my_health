package com.cnasoft.health.userservice.controller;

import com.cnasoft.health.common.exception.constant.GlobalErrorCodeConstants;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.userservice.feign.dto.ReservationConfigReqVO;
import com.cnasoft.health.userservice.service.IReservationConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author zjh
 * @date 2022/7/19
 */
@RestController
@Slf4j
@Api(tags = "预约设置api")
public class ReservationConfigController {

    @Resource
    private IReservationConfigService reservationConfigService;

    @PostMapping("/reservationConfig/create")
    @ApiOperation("新建预约设置")
    public CommonResult<Object> create(
        @RequestBody @Validated(ReservationConfigReqVO.Create.class) ReservationConfigReqVO vo) {
        reservationConfigService.create(vo);
        return CommonResult.success();
    }

    @PutMapping("/reservationConfig/update")
    @ApiOperation("编辑预约设置")
    public CommonResult<Object> update(
        @RequestBody @Validated(ReservationConfigReqVO.Update.class) ReservationConfigReqVO vo) {
        Map<String, List<Map<String, Object>>> result = reservationConfigService.update(vo);
        if (result.get("IntervalChange") != null || result.get("TimeConfigChange") != null) {
            return CommonResult.error(GlobalErrorCodeConstants.COMMON_BIZ_ERROR.getCode(), "业务异常: 设置与已有预约冲突", result);
        } else {
            return CommonResult.success();
        }
    }

    @GetMapping("/reservationConfig/get")
    @ApiOperation("根据id查询预约设置详情")
    public CommonResult<Object> get() {
        return CommonResult.success(reservationConfigService.get());
    }

    @GetMapping("/newReservation/teacher")
    @ApiOperation("查询空闲咨询师")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "date", value = "预约日期", required = true, dataType = "Integer", paramType = "query"),
        @ApiImplicitParam(name = "start", value = "预约时间段开始时间 如1300", required = true, dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "end", value = "预约时间段结束时间 如1400", dataType = "String", paramType = "query")})
    public CommonResult<Object> getAvailableTeachers(@RequestParam Map<String, Object> params) {
        return CommonResult.success(reservationConfigService.getAvailableTeachers(params));
    }

    @GetMapping("/newReservation/availableTime")
    @ApiOperation("查询咨询师空闲时间")
    public CommonResult<Object> getAvailableTimeByTeacherId(@RequestParam("psychiatristId") Long psychiatristId,
        @RequestParam("date") Long date) {
        return CommonResult.success(reservationConfigService.getAvailableTimeByTeacherId(psychiatristId, date));
    }

    @GetMapping("/newReservation/date/availableTime")
    @ApiOperation("根据日期查询当天可预约时间段")
    public CommonResult<Object> getAvailableTimeByDate(@RequestParam("date") Long date) {
        return CommonResult.success(reservationConfigService.getAvailableTimeByDate(date));
    }
}
