package com.cnasoft.health.userservice.service.impl.approve.record;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cnasoft.health.common.annotation.approve.ApproveBeanName;
import com.cnasoft.health.common.annotation.approve.ApproveService;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.enums.ApproveOperation;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.enums.ApproveType;
import com.cnasoft.health.common.util.JsonUtils;
import com.cnasoft.health.common.util.SysUserUtil;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.userservice.feign.dto.SysRoleAuthorityCreateVO;
import com.cnasoft.health.userservice.feign.dto.SysRoleUpdateReqVO;
import com.cnasoft.health.userservice.mapper.SysRoleMapper;
import com.cnasoft.health.userservice.model.SysRole;
import com.cnasoft.health.userservice.service.IApproveService;
import com.cnasoft.health.userservice.service.ISysRoleAuthorityService;
import com.cnasoft.health.userservice.service.ISysRoleService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.cnasoft.health.common.exception.constant.GlobalErrorCodeConstants.DATE_NOT_EXIST;
import static com.cnasoft.health.userservice.constant.UserErrorCodeConstants.ROLE_NOT_EXISTS;

/**
 * @author Administrator
 */
@Component(ApproveBeanName.APPROVE_ROLE)
public class ApproveSysRoleServiceImpl implements ApproveService {

    @Resource
    private IApproveService approveService;
    @Resource
    ISysRoleService roleService;
    @Resource
    SysRoleMapper roleMapper;
    @Resource
    private ISysRoleAuthorityService roleAuthorityService;

    @Override
    public void handleAddApproveRecord(Object data) {
        //修改原表审核状态为待审核
        SysRole role = (SysRole)data;
        role.setApproveStatus(ApproveStatus.TO_BE_APPROVED.getCode());
        roleMapper.updateById(role);
        role.setApproveStatus(ApproveStatus.APPROVED.getCode());
        //添加审核记录
        approveService.addApproveRecord(ApproveType.ROLE, ApproveOperation.ADD, ApproveStatus.TO_BE_APPROVED, role.getId(), SysUserUtil.getHeaderUserId(), null,
            JsonUtils.writeValueAsStringExcludeNull(role));
    }

    @Override
    public CommonResult<Object> handleUpdateApproveRecord(Object[] args) {
        Object arg = args[0];
        SysRoleUpdateReqVO updateReqVO;
        SysRole role;
        if (arg instanceof SysRoleAuthorityCreateVO) {
            SysRoleAuthorityCreateVO sysRoleAuthorityCreateVO = (SysRoleAuthorityCreateVO)args[0];
            role = roleService.getOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, sysRoleAuthorityCreateVO.getRoleCode()));
            Assert.notNull(role, ROLE_NOT_EXISTS.getMessage());
            updateReqVO = new SysRoleUpdateReqVO();
            updateReqVO.setId(role.getId());
            updateReqVO.setName(role.getName());
            updateReqVO.setAuthorities(sysRoleAuthorityCreateVO.getAuthorityCodes());
        } else {
            updateReqVO = (SysRoleUpdateReqVO)args[0];

            role = roleMapper.selectOne(new QueryWrapper<SysRole>().eq("id", updateReqVO.getId()).eq("is_deleted", 0));
            Assert.notNull(role, DATE_NOT_EXIST.getMessage());
        }
        SysRoleUpdateReqVO updateReqVOBefore = new SysRoleUpdateReqVO();
        updateReqVOBefore.setId(role.getId());
        updateReqVOBefore.setName(role.getName());
        Set<String> authorities = roleAuthorityService.findAuthoritiesByRoleId(role.getId());
        updateReqVOBefore.setAuthorities(authorities);
        String beforeJson = JsonUtils.writeValueAsStringExcludeNull(updateReqVOBefore);
        String afterJson = JsonUtils.writeValueAsStringExcludeNull(updateReqVO);
        //添加审核记录
        approveService.addApproveRecord(ApproveType.ROLE, ApproveOperation.UPDATE, ApproveStatus.TO_BE_APPROVED, updateReqVO.getId(), SysUserUtil.getHeaderUserId(), beforeJson,
            afterJson);
        return CommonResult.success();
    }

    @Override
    public CommonResult<Object> handleDeleteApproveRecord(Object[] args) {
        if (ObjectUtils.isEmpty(args)) {
            return CommonResult.success();
        }
        Set<Long> ids = (Set<Long>)args[0];
        List<BatchOperationTipDTO> resultMap = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            for (Long id : ids) {
                SysRole role = roleMapper.selectById(id);

                if (Objects.isNull(role)) {
                    resultMap.add(new BatchOperationTipDTO(id, "无效id"));
                    continue;
                }
                //添加审核记录
                approveService.addApproveRecord(ApproveType.ROLE, ApproveOperation.DELETE, ApproveStatus.TO_BE_APPROVED, id, SysUserUtil.getHeaderUserId(),
                    JsonUtils.writeValueAsStringExcludeNull(role), null);
            }
        }
        return CommonResult.success(resultMap);
    }
}
