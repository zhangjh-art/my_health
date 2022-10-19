package com.cnasoft.health.userservice.controller;

import com.cnasoft.health.common.annotation.approve.ApproveBeanName;
import com.cnasoft.health.common.annotation.approve.ApproveRecord;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.SysAreaDTO;
import com.cnasoft.health.common.enums.ApproveOperation;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.convert.SysAreaConvert;
import com.cnasoft.health.userservice.feign.dto.SysAreaReqVO;
import com.cnasoft.health.userservice.model.SysArea;
import com.cnasoft.health.userservice.service.ISysAreaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.cnasoft.health.common.vo.CommonResult.success;

/**
 * 区域管理
 *
 * @author ganghe
 */
@Slf4j
@RestController
@Api(tags = "区域模块API")
public class SysAreaController {

    @Resource
    private ISysAreaService sysAreaService;

    @GetMapping("/area/list")
    @ApiOperation(value = "获取区域列表")
    public CommonResult<Set<SysAreaDTO>> getAreaList(@RequestParam Map<String, Object> params) {
        return CommonResult.success(sysAreaService.getAreaList(params));
    }

    @GetMapping("/area/listPage")
    @ApiOperation(value = "分页获取区域列表")
    public CommonResult<PageResult<SysAreaDTO>> getAreaListPage(@RequestParam Map<String, Object> params) {
        return CommonResult.success(sysAreaService.getAreaListPage(params));
    }

    @PostMapping(value = "/area/info")
    @ApiOperation(value = "新增区域信息")
    @ApproveRecord(operation = ApproveOperation.ADD, handleServiceName = ApproveBeanName.APPROVE_AREA)
    public CommonResult<SysArea> save(@RequestBody @Validated SysAreaReqVO createReqVO) {
        SysArea sysArea = SysAreaConvert.INSTANCE.convertVO(createReqVO);
        sysArea.setApproveStatus(ApproveStatus.TO_BE_APPROVED.getCode());
        SysArea result = sysAreaService.saveArea(sysArea);
        return success(result);
    }

    @PutMapping(value = "/area/info")
    @ApiOperation(value = "修改区域信息")
    @ApproveRecord(operation = ApproveOperation.UPDATE, handleServiceName = ApproveBeanName.APPROVE_AREA)
    public CommonResult<SysArea> update(@RequestBody @Validated(SysAreaReqVO.Update.class) SysAreaReqVO updateReqVO) {
        sysAreaService.updateArea(SysAreaConvert.INSTANCE.convertVO(updateReqVO));
        return success();
    }

    @PutMapping("/area/enabled")
    @ApiOperation(value = "修改区域状态")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "用户id", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "enabled", value = "是否启用", required = true, dataType = "Boolean")})
    @ApproveRecord(operation = ApproveOperation.ENABLE_DISABLE, handleServiceName = ApproveBeanName.APPROVE_AREA)
    public CommonResult<SysArea> updateEnabled(@RequestParam Long id, @RequestParam Boolean enabled) {
        sysAreaService.updateEnabled(id, enabled);
        return success();
    }

    @DeleteMapping("/area/info")
    @ApiOperation(value = "删除指定区域")
    @ApproveRecord(operation = ApproveOperation.DELETE, handleServiceName = ApproveBeanName.APPROVE_AREA)
    public CommonResult<List<BatchOperationTipDTO>> delete(@RequestParam Set<Long> ids) {
        return success(sysAreaService.delete(ids));
    }

    @GetMapping("/area/getLastUpdateTime")
    @ApiOperation(value = "获取区域数据上次更新时间")
    public CommonResult<Long> getAreaDataLastUpdateDate() {
        return CommonResult.success(sysAreaService.getLastUpdateTime().getTime() / 1000);
    }

    @GetMapping("/area/getAreaAvailableStatus")
    @ApiOperation(value = "获取区域启用状态")
    public CommonResult<Boolean> getAreaAvailableStatus(@RequestParam Integer areaCode) {
        return CommonResult.success(sysAreaService.getAreaAvailableStatus(areaCode));
    }

    @GetMapping("/area/getArea")
    @ApiOperation(value = "获取区域信息")
    public CommonResult<SysAreaDTO> getArea(@RequestParam Integer areaCode) {
        return CommonResult.success(sysAreaService.getArea(areaCode));
    }
}
