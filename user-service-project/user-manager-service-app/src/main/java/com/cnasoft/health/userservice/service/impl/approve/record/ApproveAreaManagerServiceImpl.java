package com.cnasoft.health.userservice.service.impl.approve.record;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.cnasoft.health.common.annotation.approve.ApproveBeanName;
import com.cnasoft.health.common.annotation.approve.ApproveService;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.SysUserDTO;
import com.cnasoft.health.common.enums.ApproveOperation;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.enums.ApproveType;
import com.cnasoft.health.common.enums.RoleEnum;
import com.cnasoft.health.common.util.JsonUtils;
import com.cnasoft.health.common.util.SysUserUtil;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.userservice.feign.dto.AreaManagerUpdateReqVO;
import com.cnasoft.health.userservice.model.SysUser;
import com.cnasoft.health.userservice.service.IApproveService;
import com.cnasoft.health.userservice.service.ISysUserService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.cnasoft.health.userservice.constant.UserErrorCodeConstants.USER_NOT_EXISTS;

/**
 * @author Administrator
 */
@Component(ApproveBeanName.APPROVE_AREA_MANAGER)
public class ApproveAreaManagerServiceImpl implements ApproveService {
    @Resource
    private ISysUserService sysUserService;
    @Resource
    private IApproveService approveService;

    @Override
    public void handleAddApproveRecord(Object data) throws Exception {
        SysUser sysUser = (SysUser) data;
        sysUser.setApproveStatus(ApproveStatus.TO_BE_APPROVED.getCode());
        sysUserService.updateUserPublic(sysUser);

        // 发送审核请求
        sysUser.setPassword(null);
        sysUser.setKey(null);
        approveService.addApproveRecord(ApproveType.AREA_MANAGER, ApproveOperation.ADD, ApproveStatus.TO_BE_APPROVED, sysUser.getId(),
                SysUserUtil.getHeaderUserId(), null, JsonUtils.writeValueAsStringExcludeNull(sysUser));
    }

    @Override
    public CommonResult<Object> handleUpdateApproveRecord(Object[] args) {
        AreaManagerUpdateReqVO updateVO = (AreaManagerUpdateReqVO) args[0];
        SysUser user = sysUserService.selectSysUserById(updateVO.getId());
        Assert.notNull(user, USER_NOT_EXISTS.getMessage());
        if (StrUtil.isEmpty(updateVO.getMobile())) {
            updateVO.setMobile(user.getMobile());
        }
        updateVO.setEnabled(user.getEnabled());

        AreaManagerUpdateReqVO before = new AreaManagerUpdateReqVO();
        BeanUtils.copyProperties(user, before);

        // 发送审核请求
        if (StrUtil.isNotEmpty(updateVO.getMobile())) {
            updateVO.setMobile(updateVO.getMobile());
        }
        approveService.addApproveRecord(ApproveType.AREA_MANAGER, ApproveOperation.UPDATE, ApproveStatus.TO_BE_APPROVED, user.getId(),
                SysUserUtil.getHeaderUserId(), JsonUtils.writeValueAsStringExcludeNull(before), JsonUtils.writeValueAsStringExcludeNull(updateVO));

        return CommonResult.success();
    }

    @Override
    public CommonResult<Object> handleDeleteApproveRecord(Object[] args) {
        if (ObjectUtils.isEmpty(args)) {
            return CommonResult.success();
        }
        Set<Long> ids = (Set<Long>) args[0];
        List<BatchOperationTipDTO> resultMap = new ArrayList<>();
        if (CollUtil.isNotEmpty(ids)) {
            for (Long id : ids) {
                SysUser user = sysUserService.selectSysUserById(id);
                if (ObjectUtils.isEmpty(user)) {
                    resultMap.add(new BatchOperationTipDTO(id, "无效id"));
                    continue;
                }
                user.setPassword(null);
                user.setKey(null);
                //添加审核记录
                approveService.addApproveRecord(ApproveType.AREA_MANAGER, ApproveOperation.DELETE, ApproveStatus.TO_BE_APPROVED, id,
                        SysUserUtil.getHeaderUserId(), JsonUtils.writeValueAsStringExcludeNull(user), null);
            }
        }
        return CommonResult.success(resultMap);
    }

    @Override
    public CommonResult<Object> handleEnableApproveRecord(Object[] args) {
        Long id = (Long) args[0];
        Boolean enabled = (Boolean) args[1];
        SysUserDTO existUser = sysUserService.findByUserId(id, false);
        Assert.notNull(existUser, USER_NOT_EXISTS.getMessage());

        String roleCode = existUser.getPresetRoleCode();
        if (RoleEnum.region_admin.getValue().equals(roleCode)) {
            SysUser user = sysUserService.selectSysUserById(id);
            user.setPassword(null);
            user.setKey(null);
            String beforeJson = JsonUtils.writeValueAsStringExcludeNull(user);
            user.setEnabled(enabled);
            user.setUpdateBy(SysUserUtil.getHeaderUserId());
            user.setUpdateTime(new Date());
            String afterJson = JsonUtils.writeValueAsStringExcludeNull(user);
            ApproveOperation operation;
            if (enabled) {
                operation = ApproveOperation.ENABLE;
            } else {
                operation = ApproveOperation.DISABLE;
            }
            //添加审核记录
            approveService.addApproveRecord(ApproveType.AREA_MANAGER, operation, ApproveStatus.TO_BE_APPROVED, id,
                    SysUserUtil.getHeaderUserId(), beforeJson, afterJson);
            return CommonResult.success();
        }
        return null;
    }
}
