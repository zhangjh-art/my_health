package com.cnasoft.health.userservice.service.impl.approve.record;

import cn.hutool.core.lang.Assert;
import com.cnasoft.health.common.annotation.approve.ApproveBeanName;
import com.cnasoft.health.common.annotation.approve.ApproveService;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.enums.ApproveOperation;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.enums.ApproveType;
import com.cnasoft.health.common.util.JsonUtils;
import com.cnasoft.health.common.util.SysUserUtil;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.userservice.convert.SysAreaConvert;
import com.cnasoft.health.userservice.feign.dto.SysAreaReqVO;
import com.cnasoft.health.userservice.mapper.SysAreaMapper;
import com.cnasoft.health.userservice.model.SysArea;
import com.cnasoft.health.userservice.service.IApproveService;
import com.cnasoft.health.userservice.service.ISysAreaService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.cnasoft.health.common.exception.constant.GlobalErrorCodeConstants.BAD_REQUEST;
import static com.cnasoft.health.userservice.constant.UserErrorCodeConstants.AREA_NOT_EXISTS;

/**
 * @author Administrator
 */
@Component(ApproveBeanName.APPROVE_AREA)
public class ApproveSysAreaServiceImpl implements ApproveService {

    @Resource
    private IApproveService approveService;
    @Resource
    SysAreaMapper areaMapper;
    @Resource
    ISysAreaService areaService;


    @Override
    public void handleAddApproveRecord(Object data) {
        //修改原表审核状态为待审核
        SysArea area = (SysArea) data;
        area.setApproveStatus(ApproveStatus.TO_BE_APPROVED.getCode());
        areaMapper.updateById(area);
        area.setApproveStatus(ApproveStatus.APPROVED.getCode());
        //添加审核记录
        approveService.addApproveRecord(ApproveType.AREA, ApproveOperation.ADD, ApproveStatus.TO_BE_APPROVED, area.getId(),
                SysUserUtil.getHeaderUserId(), null, JsonUtils.writeValueAsStringExcludeNull(area));

    }

    @Override
    public CommonResult<Object> handleUpdateApproveRecord(Object[] args) {
        SysAreaReqVO updateReqVO = (SysAreaReqVO) args[0];
        SysArea area = SysAreaConvert.INSTANCE.convertVO(updateReqVO);
        SysArea oldArea = areaMapper.selectById(area.getId());

        String beforeJson = JsonUtils.writeValueAsStringExcludeNull(oldArea);
        area.setApproveStatus(ApproveStatus.APPROVED.getCode());
        String afterJson = JsonUtils.writeValueAsStringExcludeNull(area);
        //添加审核记录
        approveService.addApproveRecord(ApproveType.AREA, ApproveOperation.UPDATE, ApproveStatus.TO_BE_APPROVED, area.getId(),
                SysUserUtil.getHeaderUserId(), beforeJson, afterJson);
        return CommonResult.success();

    }

    @Override
    public CommonResult<Object> handleDeleteApproveRecord(Object[] args) {
        List<BatchOperationTipDTO> resultMap = new ArrayList<>();
        Set<Long> ids = (Set<Long>) args[0];
        if (CollectionUtils.isNotEmpty(ids)) {
            for (Long id : ids) {
                SysArea area = areaMapper.selectById(id);
                if (ObjectUtils.isEmpty(area)) {
                    resultMap.add(new BatchOperationTipDTO(id, "无效id"));
                    continue;
                }
                //添加审核记录
                approveService.addApproveRecord(ApproveType.AREA, ApproveOperation.DELETE, ApproveStatus.TO_BE_APPROVED, id,
                        SysUserUtil.getHeaderUserId(), JsonUtils.writeValueAsStringExcludeNull(area), null);
            }
        }

        return CommonResult.success(resultMap);
    }

    @Override
    public CommonResult<Object> handleEnableApproveRecord(Object[] args) {
        Long id = (Long) args[0];
        Boolean enabled = (Boolean) args[1];
        Assert.notNull(enabled, BAD_REQUEST.getMessage());
        SysArea oldArea = areaMapper.selectById(id);
        Assert.notNull(oldArea, AREA_NOT_EXISTS.getMessage());
        String beforeJson = JsonUtils.writeValueAsStringExcludeNull(oldArea);
        oldArea.setEnabled(enabled);
        String afterJson = JsonUtils.writeValueAsStringExcludeNull(oldArea);
        ApproveOperation operation;
        if (enabled) {
            operation = ApproveOperation.ENABLE;
        } else {
            operation = ApproveOperation.DISABLE;
        }
        //添加审核记录
        approveService.addApproveRecord(ApproveType.AREA, operation, ApproveStatus.TO_BE_APPROVED, oldArea.getId(),
                SysUserUtil.getHeaderUserId(), beforeJson, afterJson);
        return CommonResult.success();
    }

    @Override
    public void beforeUpdateValid(Object[] args) {
        SysAreaReqVO updateReqVO = (SysAreaReqVO) args[0];
        SysArea area = SysAreaConvert.INSTANCE.convertVO(updateReqVO);
        areaService.beforeUpdateValid(area);
    }
}
