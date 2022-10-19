package com.cnasoft.health.userservice.controller;

import cn.hutool.core.map.MapUtil;
import com.cnasoft.health.common.dto.ApproveDTO;
import com.cnasoft.health.common.dto.ApproveSimpleDTO;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.feign.dto.ApplicantUserInfoVO;
import com.cnasoft.health.userservice.feign.dto.ApproveVO;
import com.cnasoft.health.userservice.service.IApproveService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Author zcb
 * @Time 2022/4/14
 * @Package com.cnasoft.health.userservice.controller
 * @Desc 班级管理
 */
@Slf4j
@RestController
@RequestMapping("/approve")
@Api(tags = "审核模块API")
public class ApproveController {

    @Resource
    private IApproveService approveService;

    @PostMapping("/insertBatch")
    @ApiOperation(value = "批量插入审核记录")
    public CommonResult<Object> insertBatch(@RequestBody List<ApproveDTO> approves) {
        return CommonResult.success(approveService.insertBatch(approves));
    }

    @GetMapping("/list")
    @ApiOperation(value = "获取审核列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "pageNum", value = "页码", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "pageSize", value = "每页条数", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "type", value = "审核数据类型" + "2：角色审核；3：区域审核；4：学校审核；5：字典审核；6：量表审核；7：学校管理员审核；8区域管理员", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "query", value = "查询字段", dataType = "String"), @ApiImplicitParam(name = "gaugeType", value = "量表类型查询", dataType = "String"),
        @ApiImplicitParam(name = "applicantUserId", value = "申请人用户id查询", dataType = "String"), @ApiImplicitParam(name = "startTime", value = "开始时间查询", dataType = "Date"),
        @ApiImplicitParam(name = "endTime", value = "结束时间查询", dataType = "Date"), @ApiImplicitParam(name = "approveStatus", value = "审核状态查询", dataType = "Integer"),
        @ApiImplicitParam(name = "areaType", value = "区域类型查询", dataType = "Integer"), @ApiImplicitParam(name = "dictName", value = "字典名称", dataType = "String"),
        @ApiImplicitParam(name = "dictType", value = "字典类型", dataType = "String"),})
    public CommonResult<PageResult<ApproveVO>> list(@RequestParam Map<String, Object> params) {
        return CommonResult.success(approveService.listByParam(params));
    }

    @GetMapping("/applicantUserList")
    @ApiOperation(value = "获取申请人列表")
    @ApiImplicitParam(name = "type", value = "数据类型，同列表接口", dataType = "Integer")
    public CommonResult<List<ApplicantUserInfoVO>> getApplicantUserList(@RequestParam(value = "type", required = false) String type) {
        return CommonResult.success(approveService.getApplicantUserList(type));
    }

    @GetMapping("/allow")
    @ApiOperation(value = "通过审核")
    public CommonResult<BatchOperationTipDTO> allow(@RequestParam("id") Long recordId) {
        return CommonResult.success(approveService.handleApprove(recordId, true, null));
    }

    @PostMapping("/batchAllow")
    @ApiOperation(value = "批量通过审核")
    public CommonResult<List<BatchOperationTipDTO>> batchAllow(@RequestBody List<Long> recordIds) {
        List<BatchOperationTipDTO> resultList = new ArrayList<>(recordIds.size());
        for (Long recordId : recordIds) {
            BatchOperationTipDTO result = approveService.handleApprove(recordId, true, null);
            if (Objects.nonNull(result)) {
                resultList.add(result);
            }
        }
        return CommonResult.success(resultList);
    }

    @PostMapping("/reject")
    @ApiOperation(value = "拒绝审核申请")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "审核记录id", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "rejectReason", value = "拒绝原因", required = true, dataType = "Integer"),})
    public CommonResult<BatchOperationTipDTO> reject(@RequestBody Map<String, Object> params) {
        return CommonResult.success(approveService.handleApprove(MapUtil.getLong(params, "id"), false, MapUtil.getStr(params, "rejectReason")));
    }

    /**
     * 查询资源审核的数量
     *
     * @param approveType
     * @param operation
     * @param approveStatus
     * @param resourceIds
     * @return
     */
    @PostMapping(value = "select-approve-count")
    public CommonResult<Integer> selectApproveCount(@RequestParam("approveType") Integer approveType, @RequestParam("operation") Integer operation,
        @RequestParam("approveStatus") Integer approveStatus, @RequestParam("resourceIds") List<Long> resourceIds) {
        return approveService.selectApproveCount(approveType, operation, approveStatus, resourceIds);
    }

    @GetMapping(value = "/approveList")
    @ApiOperation(value = "获取最新的审核记录信息列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "approveType", value = "审核数据类型", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "businessIds", value = "数据id", required = true, dataType = "Integer"),})
    public CommonResult<List<ApproveSimpleDTO>> getApproveList(@RequestParam("approveType") Integer approveType, @RequestParam("businessIds") List<Long> businessIds) {
        return CommonResult.success(approveService.getApproveList(approveType, businessIds));
    }
}
