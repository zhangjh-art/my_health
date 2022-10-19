package com.cnasoft.health.userservice.service.impl.approve.handler;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import com.cnasoft.health.common.annotation.approve.ApproveBeanName;
import com.cnasoft.health.common.dto.SysRoleDTO;
import com.cnasoft.health.common.enums.ApproveOperation;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.util.JsonUtils;
import com.cnasoft.health.userservice.feign.dto.ApproveVO;
import com.cnasoft.health.userservice.feign.dto.SysRoleUpdateReqVO;
import com.cnasoft.health.userservice.mapper.SysRoleMapper;
import com.cnasoft.health.userservice.model.Approve;
import com.cnasoft.health.userservice.model.SysRole;
import com.cnasoft.health.userservice.service.ISysRoleAuthorityService;
import com.cnasoft.health.userservice.service.ISysRoleService;
import com.google.common.collect.Sets;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.cnasoft.health.common.exception.constant.GlobalErrorCodeConstants.BAD_REQUEST;

/**
 * @author Administrator
 */
@Service(ApproveBeanName.APPROVE_ROLE_HANDLER)
public class ApproveRoleHandleServiceImpl implements ApproveHandleService {
    @Resource
    ISysRoleService sysRoleService;
    @Resource
    SysRoleMapper roleMapper;
    @Resource
    private ISysRoleAuthorityService roleAuthorityService;

    @Override
    public void handleAddApprove(Approve approve, boolean allow) throws Exception {
        SysRole role = roleMapper.selectById(approve.getBusinessId());
        Assert.notNull(role, BAD_REQUEST.getMessage());
        role.setApproveStatus(allow ? ApproveStatus.APPROVED.getCode() : ApproveStatus.REJECTED.getCode());
        roleMapper.updateById(role);
    }

    @Override
    public void handleDeleteApprove(Approve approve) {
        SysRole role = JsonUtils.readValue(approve.getBeforeString(), SysRole.class);
        assert role != null;
        Assert.notNull(role, BAD_REQUEST.getMessage());
        sysRoleService.deleteRole(Sets.newHashSet(approve.getBusinessId()));
    }

    @Override
    public void handleUpdateApprove(Approve approve) {
        SysRoleUpdateReqVO role = JsonUtils.readValue(approve.getAfterString(), SysRoleUpdateReqVO.class);
        assert role != null;
        Assert.notNull(role, BAD_REQUEST.getMessage());
        sysRoleService.updateRole(role);
    }

    @Override
    public List<Long> queryApproveBusinessId(Map<String, Object> params) {
        String query = MapUtil.getStr(params, "query");
        if (StringUtils.isEmpty(query)) {
            return null;
        }
        List<Long> result = roleMapper.getApproveAreaId(query);
        return result == null ? new ArrayList<>() : result;
    }

    @Override
    public void handleQueryResult(ApproveVO approve) {
        Integer operation = approve.getApproveOperation();
        approve.setBeforeJson(handleQueryResult(approve.getBeforeJson(), operation));
        approve.setAfterJson(handleQueryResult(approve.getAfterJson(), operation));
    }

    private String handleQueryResult(String json, Integer operation) {
        if (StringUtils.isEmpty(json)) {
            return json;
        }
        SysRoleDTO sysRoleDTO = new SysRoleDTO();
        if (ApproveOperation.ADD.getCode().equals(operation)
                || ApproveOperation.DELETE.getCode().equals(operation)) {
            SysRole role = JsonUtils.readValue(json, SysRole.class);
            assert role != null : BAD_REQUEST;
            BeanUtils.copyProperties(role, sysRoleDTO);
            Set<String> authorities = roleAuthorityService.findAuthoritiesByRoleId(sysRoleDTO.getId());
            sysRoleDTO.setAuthorities(authorities);
        } else if (ApproveOperation.UPDATE.getCode().equals(operation)) {
            SysRoleUpdateReqVO role = JsonUtils.readValue(json, SysRoleUpdateReqVO.class);
            assert role != null : BAD_REQUEST;
            BeanUtils.copyProperties(role, sysRoleDTO);
            sysRoleDTO.setAuthorities(role.getAuthorities());
        } else {
            return json;
        }
        if (StringUtils.isEmpty(sysRoleDTO.getCode())) {
            SysRole role = roleMapper.findById(sysRoleDTO.getId());
            if (Objects.nonNull(role)) {
                sysRoleDTO.setCode(role.getCode());
            }
        }

        return JsonUtils.writeValueAsString(sysRoleDTO);
    }

}
