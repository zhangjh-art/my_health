package com.cnasoft.health.userservice.controller;

import cn.hutool.http.HttpStatus;
import com.cnasoft.health.common.annotation.approve.ApproveBeanName;
import com.cnasoft.health.common.annotation.approve.ApproveRecord;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.SysAuthorityDTO;
import com.cnasoft.health.common.enums.ApproveOperation;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.feign.dto.SysAuthorityReqVO;
import com.cnasoft.health.userservice.feign.dto.SysRoleAuthorityCreateVO;
import com.cnasoft.health.userservice.service.ISysAuthorityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.cnasoft.health.common.vo.CommonResult.error;
import static com.cnasoft.health.common.vo.CommonResult.success;

/**
 * @author ganghe
 */
@Slf4j
@RestController
@Api(tags = "权限模块API")
public class SysAuthorityController {

    @Resource
    private ISysAuthorityService authorityService;

    @GetMapping("/authority/list")
    @ApiOperation(value = "查询权限列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页码", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "query", value = "ID/权限名称", dataType = "String"),
            @ApiImplicitParam(name = "enabled", value = "启用/禁用状态", dataType = "Boolean")
    })
    public CommonResult<PageResult<SysAuthorityDTO>> list(@RequestParam Map<String, Object> params) {
        return CommonResult.success(authorityService.findList(params));
    }

    @GetMapping("/authority/list/all")
    @ApiOperation(value = "查询所有权限")
    public CommonResult<List<SysAuthorityDTO>> listAll() {
        return success(authorityService.findAll());
    }

    @GetMapping("/authority/list/ids")
    @ApiOperation(value = "根据roleIds查询权限列表")
    public CommonResult<List<SysAuthorityDTO>> findAuthorityByRoleId(@RequestParam String roleIds) {
        if (StringUtils.isBlank(roleIds)) {
            return error(HttpStatus.HTTP_BAD_REQUEST, "角色ID不能为空");
        }

        return success(authorityService.findByRoles(Collections.singleton(Long.parseLong(roleIds))));
    }

    @GetMapping("/authority/list/codes")
    @ApiOperation(value = "根据roleCodes查询权限列表")
    public CommonResult<List<SysAuthorityDTO>> findMenuByRoles(@RequestParam String roleCodes) {
        if (StringUtils.isBlank(roleCodes)) {
            return error(HttpStatus.HTTP_BAD_REQUEST, "角色编码不能为空");
        }

        return success(authorityService.findByRoleCodes(Collections.singleton(roleCodes)));
    }

    @PostMapping("/authority/info/assign")
    @ApiOperation(value = "角色分配权限")
    @ApproveRecord(operation = ApproveOperation.UPDATE, handleServiceName = ApproveBeanName.APPROVE_ROLE)
    public CommonResult<SysAuthorityDTO> setAuthorityToRole(@RequestBody @Validated SysRoleAuthorityCreateVO sysRoleAuthorityCreateVO) {
        authorityService.setAuthorityToRole(sysRoleAuthorityCreateVO);
        return success();
    }

    @PostMapping("/authority/info")
    @ApiOperation(value = "新增权限")
    public CommonResult<SysAuthorityDTO> save(@RequestBody @Validated SysAuthorityReqVO reqVO) {
        authorityService.createAuthority(reqVO);
        return success();
    }

    @PutMapping("/authority/info")
    @ApiOperation(value = "修改权限")
    public CommonResult<SysAuthorityDTO> update(@RequestBody @Validated(SysAuthorityReqVO.Update.class) SysAuthorityReqVO reqVO) {
        authorityService.updateAuthority(reqVO);
        return success();
    }

    @DeleteMapping("/authority/info")
    @ApiOperation(value = "删除指定权限")
    public CommonResult<List<BatchOperationTipDTO>> deleteAuthorities(@RequestParam Set<Long> ids) {
        return success(authorityService.delete(ids));
    }

    @PutMapping("/authority/enabled")
    @ApiOperation(value = "修改权限状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "权限id", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "enabled", value = "是否启用", required = true, dataType = "Boolean")
    })
    public CommonResult<SysAuthorityDTO> setStatus(@RequestParam Long id, @RequestParam boolean enabled) {
        authorityService.updateEnabled(id, enabled);
        return success();
    }
}
