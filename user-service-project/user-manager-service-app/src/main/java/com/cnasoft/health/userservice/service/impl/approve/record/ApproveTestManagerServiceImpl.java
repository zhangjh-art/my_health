package com.cnasoft.health.userservice.service.impl.approve.record;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.cnasoft.health.common.annotation.approve.ApproveBeanName;
import com.cnasoft.health.common.annotation.approve.ApproveService;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.SchoolDTO;
import com.cnasoft.health.common.dto.SysUserDTO;
import com.cnasoft.health.common.enums.ApproveOperation;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.enums.ApproveType;
import com.cnasoft.health.common.enums.RoleEnum;
import com.cnasoft.health.common.util.JsonUtils;
import com.cnasoft.health.common.util.SysUserUtil;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.userservice.feign.dto.TestManagerReqVO;
import com.cnasoft.health.userservice.mapper.SysUserMapper;
import com.cnasoft.health.userservice.mapper.TestManagerMapper;
import com.cnasoft.health.userservice.model.SysUser;
import com.cnasoft.health.userservice.service.IApproveService;
import com.cnasoft.health.userservice.service.ISysUserService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.cnasoft.health.userservice.constant.UserErrorCodeConstants.USER_NOT_EXISTS;

/**
 * 测试管理员-操作审核记录
 *
 * @author ganghe
 */
@Component(ApproveBeanName.APPROVE_TEST_MANAGER)
public class ApproveTestManagerServiceImpl implements ApproveService {
    @Resource
    private SysUserMapper userMapper;
    @Resource
    private ISysUserService sysUserService;
    @Resource
    private TestManagerMapper testManagerMapper;
    @Resource
    private IApproveService approveService;

    @Override
    public void handleAddApproveRecord(Object data) {
        TestManagerReqVO after = (TestManagerReqVO)data;

        //添加审核记录
        approveService.addApproveRecord(ApproveType.TEST_MANAGER, ApproveOperation.ADD, ApproveStatus.TO_BE_APPROVED, after.getId(), SysUserUtil.getHeaderUserId(), null,
            JsonUtils.writeValueAsStringExcludeNull(after));
    }

    @Override
    public CommonResult<Object> handleUpdateApproveRecord(Object[] args) {
        TestManagerReqVO after = (TestManagerReqVO)args[0];
        SysUser sysUser = sysUserService.selectSysUserById(after.getId());

        TestManagerReqVO before = new TestManagerReqVO();
        BeanUtils.copyProperties(sysUser, before);

        Long userId = sysUser.getId();
        List<SchoolDTO> beforeSchools = testManagerMapper.findSchoolList(userId, null);
        if (CollUtil.isNotEmpty(beforeSchools)) {
            before.setSchools(beforeSchools);
            if (CollUtil.isEmpty(after.getSchools())) {
                after.setSchools(beforeSchools);
            }
        }

        if (StringUtils.isBlank(after.getName())) {
            after.setName(before.getName());
        }
        if (null == after.getSex()) {
            after.setSex(before.getSex());
        }
        if (StringUtils.isBlank(after.getMobile())) {
            after.setMobile(before.getMobile());
        }
        if (StringUtils.isBlank(after.getEmail())) {
            after.setEmail(before.getEmail());
        }
        if (null == after.getEnabled()) {
            after.setEnabled(before.getEnabled());
        }

        //添加审核记录
        approveService.addApproveRecord(ApproveType.TEST_MANAGER, ApproveOperation.UPDATE, ApproveStatus.TO_BE_APPROVED, userId, SysUserUtil.getHeaderUserId(),
            JsonUtils.writeValueAsStringExcludeNull(before), JsonUtils.writeValueAsStringExcludeNull(after));
        return CommonResult.success();
    }

    @Override
    public CommonResult<Object> handleDeleteApproveRecord(Object[] args) {
        if (ObjectUtils.isEmpty(args)) {
            return CommonResult.success();
        }

        Set<Long> ids = (HashSet<Long>)args[0];
        List<BatchOperationTipDTO> resultMap = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            for (Long id : ids) {
                SysUser sysUser = sysUserService.selectSysUserById(id);

                TestManagerReqVO before = new TestManagerReqVO();
                BeanUtils.copyProperties(sysUser, before);

                Long userId = sysUser.getId();
                List<SchoolDTO> beforeSchools = testManagerMapper.findSchoolList(userId, null);
                if (CollUtil.isNotEmpty(beforeSchools)) {
                    before.setSchools(beforeSchools);
                }

                //添加审核记录
                approveService.addApproveRecord(ApproveType.TEST_MANAGER, ApproveOperation.DELETE, ApproveStatus.TO_BE_APPROVED, id, SysUserUtil.getHeaderUserId(),
                    JsonUtils.writeValueAsStringExcludeNull(before), null);
            }
        }
        return CommonResult.success(resultMap);
    }

    @Override
    public CommonResult<Object> handleEnableApproveRecord(Object[] args) {
        Long id = (Long)args[0];
        Boolean enabled = (Boolean)args[1];
        SysUserDTO existUser = sysUserService.findByUserId(id, false);
        Assert.notNull(existUser, USER_NOT_EXISTS.getMessage());

        String roleCode = existUser.getPresetRoleCode();
        if (RoleEnum.test_admin.getValue().equals(roleCode)) {
            SysUser sysUser = sysUserService.selectSysUserById(id);

            TestManagerReqVO before = new TestManagerReqVO();
            BeanUtils.copyProperties(sysUser, before);

            Long userId = sysUser.getId();
            List<SchoolDTO> beforeSchools = testManagerMapper.findSchoolList(userId, null);
            if (CollUtil.isNotEmpty(beforeSchools)) {
                before.setSchools(beforeSchools);
            }

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
            approveService.addApproveRecord(ApproveType.TEST_MANAGER, operation, ApproveStatus.TO_BE_APPROVED, id, SysUserUtil.getHeaderUserId(), beforeJson, afterJson);
            return CommonResult.success();
        }
        return null;
    }
}
