package com.cnasoft.health.userservice.controller;

import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.userservice.feign.dto.NewReservationReqVO;
import com.cnasoft.health.userservice.feign.dto.SupplementReservationReqVO;
import com.cnasoft.health.userservice.service.INewReservationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author zjh
 * @date 2022/7/19
 */
@RestController
@Slf4j
@Api(tags = "新预约api")
public class NewReservationController {

    @Resource
    private INewReservationService reservationService;

    @PostMapping("/newReservation/create")
    @ApiOperation("新建预约")
    public CommonResult<Object> create(@RequestBody @Validated(NewReservationReqVO.Create.class) NewReservationReqVO vo) throws Exception {
        reservationService.createLocked(vo, 0);
        return CommonResult.success();
    }

    @PostMapping("/newReservation/substituted/create")
    @ApiOperation("代预约")
    public CommonResult<Object> substitutedCreate(@RequestBody @Validated(NewReservationReqVO.Create.class) NewReservationReqVO vo) throws Exception {
        reservationService.createLocked(vo, 1);
        return CommonResult.success();
    }

    @PostMapping("/newReservation/supplement/create")
    @ApiOperation("补充预约")
    public CommonResult<Object> supplementCreate(@RequestBody @Validated(SupplementReservationReqVO.Create.class) SupplementReservationReqVO vo) throws Exception {
        reservationService.supplementCreate(vo);
        return CommonResult.success();
    }

    @PutMapping("/newReservation/update")
    @ApiOperation("编辑预约")
    public CommonResult<Object> update(@RequestBody @Validated(NewReservationReqVO.Update.class) NewReservationReqVO vo) throws Exception {
        reservationService.updateLocked(vo);
        return CommonResult.success();
    }

    @GetMapping("/newReservation/get")
    @ApiOperation("根据id查询预约详情")
    public CommonResult<Object> get(@RequestParam("id") Long id) {
        return CommonResult.success(reservationService.get(id));
    }

    @GetMapping("/newReservation/user")
    @ApiOperation("代预约查询预约用户")
    @ApiImplicitParams({@ApiImplicitParam(name = "userRoleType", value = "用户类型 0:学生 1:家长 2:教职工 3:区域职员", required = true, dataType = "Integer", paramType = "query"),
        @ApiImplicitParam(name = "name", value = "用户名称", dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "identityCardNumber", value = "身份证号", dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "department", value = "教职工部门", dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "studentNumber", value = "学号", dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "clazzId", value = "班级", dataType = "Long", paramType = "query")})
    public CommonResult<Object> getReservationUserList(@RequestParam Map<String, Object> params) {
        return CommonResult.success(reservationService.getReservationUserList(params));
    }

    @GetMapping("/newReservation/list")
    @ApiOperation("教师条件查询预约")
    @ApiImplicitParams({@ApiImplicitParam(name = "pageNum", value = "页码", required = true, dataType = "Integer", paramType = "query"),
        @ApiImplicitParam(name = "pageSize", value = "每页条数", required = true, dataType = "Integer", paramType = "query"),
        @ApiImplicitParam(name = "date", value = "查询日期", dataType = "Integer", paramType = "query"),
        @ApiImplicitParam(name = "userName", value = "用户名称", dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "userRoleType", value = "用户类型", dataType = "Integer", paramType = "query"),
        @ApiImplicitParam(name = "status", value = "状态", dataType = "Integer", paramType = "query"),
        @ApiImplicitParam(name = "id", value = "id", dataType = "Long", paramType = "query")})
    public CommonResult<Object> list(@RequestParam Map<String, Object> params) {
        return CommonResult.success(reservationService.list(params, true));
    }

    @GetMapping("/newReservation/student/list")
    @ApiOperation("学生条件查询预约")
    @ApiImplicitParams({@ApiImplicitParam(name = "pageNum", value = "页码", required = true, dataType = "Integer", paramType = "query"),
        @ApiImplicitParam(name = "pageSize", value = "每页条数", required = true, dataType = "Integer", paramType = "query"),
        @ApiImplicitParam(name = "psychiatristName", value = "咨询师名字", dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "status", value = "状态", dataType = "Integer", paramType = "query")})
    public CommonResult<Object> studentReservationList(@RequestParam Map<String, Object> params) {
        return CommonResult.success(reservationService.list(params, false));
    }

    @GetMapping("/newReservation/statistical")
    @ApiOperation("获取预约总览数据")
    @ApiImplicitParams({@ApiImplicitParam(name = "year", value = "年份(默认当年)", dataType = "Integer", paramType = "query"),
        @ApiImplicitParam(name = "month", value = "月份(默认当月)", dataType = "Integer", paramType = "query")})
    public CommonResult<Object> getStatisticalData(@RequestParam(value = "year", required = false) Integer year, @RequestParam(value = "month", required = false) Integer month) {
        return CommonResult.success(reservationService.getStatisticalData(year, month));
    }

    @GetMapping("/newReservation/update/status")
    @ApiOperation("更新预约状态")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "canceledByStudent", value = "更改状态为取消时需要, 0否 1是", dataType = "Integer", paramType = "query")})
    public CommonResult<Object> updateStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status,
        @RequestParam(value = "remark", required = false) String remark,
        @RequestParam(value = "cancelOtherReason", required = false) String cancelOtherReason) throws Exception {
        reservationService.updateStatusLocked(id, status, remark, cancelOtherReason);
        return CommonResult.success();
    }

    @GetMapping("/newReservation/toConfirmed")
    @ApiOperation("查询心理老师待确认预约")
    public CommonResult<Object> getToConfirmedTask() {
        return CommonResult.success(reservationService.getToConfirmedTask());
    }

    @GetMapping("/newReservation/confirmed")
    @ApiOperation("查询用户已确认预约")
    public CommonResult<Object> getConfirmedTask() {
        return CommonResult.success(reservationService.getConfirmedTask());
    }
}
