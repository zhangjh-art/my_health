package com.cnasoft.health.userservice.controller;

import com.cnasoft.health.common.annotation.approve.ApproveBeanName;
import com.cnasoft.health.common.annotation.approve.ApproveRecord;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.SysRoleDTO;
import com.cnasoft.health.common.enums.ApproveOperation;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.feign.dto.SysRoleCreateReqVO;
import com.cnasoft.health.userservice.feign.dto.SysRoleUpdateReqVO;
import com.cnasoft.health.userservice.model.SysRole;
import com.cnasoft.health.userservice.service.ISysRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
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
 * @author cnasoft
 * @date 2020/8/16 12:03
 */
@Slf4j
@RestController
@Api(tags = "角色模块API")
public class SysRoleController {
    @Resource
    private ISysRoleService sysRoleService;

    @GetMapping("/role/list")
    @ApiOperation(value = "按参数查询角色")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页码", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "code", value = "角色编码", dataType = "String"),
            @ApiImplicitParam(name = "enabled", value = "启用/禁用", dataType = "Boolean"),
            @ApiImplicitParam(name = "createDate", value = "创建时间", dataType = "Integer")
    })
    public CommonResult<PageResult<SysRoleDTO>> findRoles(@RequestParam Map<String, Object> params) {
        return success(sysRoleService.findRoles(params));
    }

    @GetMapping("/role/list/all")
    @ApiOperation(value = "查询所有角色")
    public CommonResult<List<SysRoleDTO>> findAll() {
        return success(sysRoleService.findAll());
    }

    @PostMapping("/role/info")
    @ApiOperation(value = "新增角色")
    @ApproveRecord(operation = ApproveOperation.ADD, handleServiceName = ApproveBeanName.APPROVE_ROLE)
    public CommonResult<SysRole> saveRole(@RequestBody SysRoleCreateReqVO createReqVO) throws Exception {
        SysRole sysRole = sysRoleService.saveRole(createReqVO);
        return success(sysRole);
    }

    @PutMapping("/role/info")
    @ApiOperation(value = "修改角色")
    @ApproveRecord(operation = ApproveOperation.UPDATE, handleServiceName = ApproveBeanName.APPROVE_ROLE)
    public CommonResult<SysRole> updateRole(@RequestBody SysRoleUpdateReqVO updateReqVO) {
        sysRoleService.updateRole(updateReqVO);
        return success();
    }

    @DeleteMapping("/role/info")
    @ApiOperation(value = "删除角色")
    @ApproveRecord(operation = ApproveOperation.DELETE, handleServiceName = ApproveBeanName.APPROVE_ROLE)
    public CommonResult<List<BatchOperationTipDTO>> deleteRole(@RequestParam Set<Long> ids) {
        return success(sysRoleService.deleteRole(ids));
    }
}
