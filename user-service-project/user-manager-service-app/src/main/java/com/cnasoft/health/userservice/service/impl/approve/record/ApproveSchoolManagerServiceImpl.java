package com.cnasoft.health.userservice.service.impl.approve.record;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.cnasoft.health.userservice.feign.dto.SchoolManagerUpdateReqVO;
import com.cnasoft.health.userservice.mapper.SchoolManagerMapper;
import com.cnasoft.health.userservice.mapper.SchoolMapper;
import com.cnasoft.health.userservice.mapper.SysUserMapper;
import com.cnasoft.health.userservice.model.School;
import com.cnasoft.health.userservice.model.SchoolManager;
import com.cnasoft.health.userservice.model.SysUser;
import com.cnasoft.health.userservice.service.IApproveService;
import com.cnasoft.health.userservice.service.ISysUserService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.cnasoft.health.userservice.constant.UserErrorCodeConstants.USER_NOT_EXISTS;

/**
 * @author Administrator
 */
@Component(ApproveBeanName.APPROVE_SCHOOL_MANAGER)
public class ApproveSchoolManagerServiceImpl implements ApproveService {

    @Resource
    private SysUserMapper userMapper;
    @Resource
    private SchoolMapper schoolMapper;
    @Resource
    private ISysUserService sysUserService;
    @Resource
    private SchoolManagerMapper schoolManagerMapper;
    @Resource
    private IApproveService approveService;


    @Override
    public void handleAddApproveRecord(Object data) throws Exception {
        //修改原表审核状态为待审核
        SchoolManager manager = (SchoolManager) data;
        manager.setApproveStatus(ApproveStatus.TO_BE_APPROVED.getCode());
        schoolManagerMapper.updateById(manager);
        manager.setApproveStatus(ApproveStatus.APPROVED.getCode());

        SysUser sysUser = sysUserService.selectSysUserById(manager.getUserId());
        sysUser.setApproveStatus(ApproveStatus.TO_BE_APPROVED.getCode());
        sysUserService.updateUserPublic(sysUser);

        SchoolManagerUpdateReqVO after = new SchoolManagerUpdateReqVO();
        BeanUtils.copyProperties(sysUser, after);
        School school = userMapper.findAreaCode(sysUser.getId());
        after.setAreaCode(school.getAreaCode());
        after.setSchoolId(manager.getSchoolId());
        after.setSchoolName(schoolMapper.selectById(manager.getSchoolId()).getName());

        //添加审核记录
        approveService.addApproveRecord(ApproveType.SCHOOL_MANAGER, ApproveOperation.ADD, ApproveStatus.TO_BE_APPROVED, manager.getUserId(),
                SysUserUtil.getHeaderUserId(), null, JsonUtils.writeValueAsStringExcludeNull(after));
    }

    @Override
    public CommonResult<Object> handleUpdateApproveRecord(Object[] args) {

        SchoolManagerUpdateReqVO after = (SchoolManagerUpdateReqVO) args[0];
        SysUser sysUser = sysUserService.selectSysUserById(after.getId());

        SchoolManagerUpdateReqVO before = new SchoolManagerUpdateReqVO();
        BeanUtils.copyProperties(sysUser, before);

        SchoolManager beforeManager = schoolManagerMapper.selectOne(
                new LambdaQueryWrapper<SchoolManager>().eq(SchoolManager::getUserId, sysUser.getId()));
        if (Objects.nonNull(beforeManager)) {
            Long beforeSchoolId = beforeManager.getSchoolId();
            School beforeSchool = schoolMapper.selectById(beforeSchoolId);
            before.setSchoolId(beforeSchoolId);
            before.setSchoolName(beforeSchool.getName());
            before.setAreaCode(beforeSchool.getAreaCode());

            Long afterSchoolId = after.getSchoolId();
            School afterSchool = schoolMapper.selectById(afterSchoolId);
            after.setSchoolName(afterSchool.getName());
            after.setAreaCode(afterSchool.getAreaCode());
        }

        if (after.getEnabled() == null) {
            after.setEnabled(before.getEnabled());
        }

        approveService.addApproveRecord(ApproveType.SCHOOL_MANAGER, ApproveOperation.UPDATE, ApproveStatus.TO_BE_APPROVED, beforeManager.getUserId(),
                SysUserUtil.getHeaderUserId(), JsonUtils.writeValueAsStringExcludeNull(before), JsonUtils.writeValueAsStringExcludeNull(after));
        return CommonResult.success();
    }

    @Override
    public CommonResult<Object> handleDeleteApproveRecord(Object[] args) {
        if (ObjectUtils.isEmpty(args)) {
            return CommonResult.success();
        }
        Set<Long> ids = (HashSet<Long>) args[0];
        List<BatchOperationTipDTO> resultMap = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            for (Long id : ids) {
                SchoolManager manager = schoolManagerMapper.selectOne(new LambdaQueryWrapper<SchoolManager>().eq(SchoolManager::getUserId, id));
                if (Objects.isNull(manager)) {
                    resultMap.add(new BatchOperationTipDTO(id, "无效id"));
                    continue;
                }
                SysUser sysUser = sysUserService.selectSysUserById(id);

                SchoolManagerUpdateReqVO before = new SchoolManagerUpdateReqVO();
                BeanUtils.copyProperties(sysUser, before);
                before.setSchoolId(manager.getSchoolId());
                School beforeSchool = schoolMapper.selectById(manager.getSchoolId());
                before.setSchoolName(beforeSchool.getName());
                before.setAreaCode(beforeSchool.getAreaCode());
                //添加审核记录
                approveService.addApproveRecord(ApproveType.SCHOOL_MANAGER, ApproveOperation.DELETE, ApproveStatus.TO_BE_APPROVED, id,
                        SysUserUtil.getHeaderUserId(), JsonUtils.writeValueAsStringExcludeNull(before), null);
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
        if (RoleEnum.school_admin.getValue().equals(roleCode)) {
            SchoolManager manager = schoolManagerMapper.selectOne(new LambdaQueryWrapper<SchoolManager>().eq(SchoolManager::getUserId, id));
                Assert.notNull(manager,"无效id");
            SysUser sysUser = sysUserService.selectSysUserById(id);

            SchoolManagerUpdateReqVO before = new SchoolManagerUpdateReqVO();
            BeanUtils.copyProperties(sysUser, before);
            before.setSchoolId(manager.getSchoolId());
            School beforeSchool = schoolMapper.selectById(manager.getSchoolId());
            before.setSchoolName(beforeSchool.getName());
            before.setAreaCode(beforeSchool.getAreaCode());
            String beforeJson = JsonUtils.writeValueAsStringExcludeNull(before);
            before.setEnabled(enabled);
            String afterJson = JsonUtils.writeValueAsStringExcludeNull(before);

            ApproveOperation operation;
            if (enabled) {
                operation = ApproveOperation.ENABLE;
            } else {
                operation = ApproveOperation.DISABLE;
            }
            //添加审核记录
            approveService.addApproveRecord(ApproveType.SCHOOL_MANAGER, operation, ApproveStatus.TO_BE_APPROVED, id,
                    SysUserUtil.getHeaderUserId(), beforeJson, afterJson);
            return CommonResult.success();
        }
        return null;
    }

}
