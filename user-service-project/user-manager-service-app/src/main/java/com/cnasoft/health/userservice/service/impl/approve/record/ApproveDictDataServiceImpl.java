package com.cnasoft.health.userservice.service.impl.approve.record;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.cnasoft.health.common.annotation.approve.ApproveBeanName;
import com.cnasoft.health.common.annotation.approve.ApproveService;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.SysDictDTO;
import com.cnasoft.health.common.enums.ApproveOperation;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.enums.ApproveType;
import com.cnasoft.health.common.enums.DictType;
import com.cnasoft.health.common.util.JsonUtils;
import com.cnasoft.health.common.util.SysUserUtil;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.userservice.feign.TaskFeign;
import com.cnasoft.health.userservice.mapper.SysDictDataMapper;
import com.cnasoft.health.userservice.model.SysDictData;
import com.cnasoft.health.userservice.service.IApproveService;
import com.cnasoft.health.userservice.service.ISysDictService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.cnasoft.health.common.exception.util.ServiceExceptionUtil.exception;

/**
 * @author Administrator
 */
@Component(ApproveBeanName.APPROVE_DICT_DATA)
public class ApproveDictDataServiceImpl implements ApproveService {
    @Resource
    private SysDictDataMapper sysDictDataMapper;
    @Resource
    private IApproveService approveService;
    @Resource
    private ISysDictService sysDictService;
    @Resource
    private TaskFeign taskFeign;

    @Override
    public void handleAddApproveRecord(Object data) {
        SysDictData sysDictData = (SysDictData)data;
        //添加审核记录
        approveService.addApproveRecord(ApproveType.DEPARTMENT, ApproveOperation.ADD, ApproveStatus.TO_BE_APPROVED, sysDictData.getId(), SysUserUtil.getHeaderUserId(), null,
            JsonUtils.writeValueAsStringExcludeNull(sysDictData));
    }

    @Override
    public CommonResult<Object> handleUpdateApproveRecord(Object[] args) {
        SysDictDTO dictDTO = (SysDictDTO)args[0];
        SysDictData sysDictData = sysDictDataMapper.selectById(dictDTO.getId());
        if (dictDTO.getDisable() != null && !dictDTO.getDisable().equals(sysDictData.getDisable())) {
            return handleEnableApproveRecord(args);
        }

        String beforeJson = JsonUtils.writeValueAsStringExcludeNull(sysDictData);
        SysDictData afterData = new SysDictData();
        BeanUtil.copyProperties(sysDictData, afterData);
        afterData.setDictType(dictDTO.getDictType());
        afterData.setDictName(dictDTO.getDictName());
        if (Objects.nonNull(dictDTO.getDisable())) {
            afterData.setDisable(dictDTO.getDisable());
        }
        String afterJson = JsonUtils.writeValueAsStringExcludeNull(afterData);

        //添加审核记录
        approveService.addApproveRecord(ApproveType.DEPARTMENT, ApproveOperation.UPDATE, ApproveStatus.TO_BE_APPROVED, dictDTO.getId(), SysUserUtil.getHeaderUserId(), beforeJson,
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
        if (CollUtil.isNotEmpty(ids)) {
            for (Long id : ids) {
                SysDictData dictData = sysDictDataMapper.selectById(id);
                if (ObjectUtils.isEmpty(dictData)) {
                    resultMap.add(new BatchOperationTipDTO(id, "无效id"));
                    continue;
                }
                if (DictType.ScaleType.getCode().equals(dictData.getDictType())) {
                    List<Long> gaugeIds = taskFeign.getGaugeIdByQuery(null, dictData.getDictValue(), 0).getData();
                    if (CollectionUtils.isNotEmpty(gaugeIds)) {
                        resultMap.add(new BatchOperationTipDTO(id, "量表类型:" + dictData.getDictName() + " 已被使用，不能删除"));
                        continue;
                    }
                }
                //添加审核记录
                approveService.addApproveRecord(ApproveType.DEPARTMENT, ApproveOperation.DELETE, ApproveStatus.TO_BE_APPROVED, id, SysUserUtil.getHeaderUserId(),
                    JsonUtils.writeValueAsStringExcludeNull(dictData), null);
            }
        }

        return CommonResult.success(resultMap);
    }

    @Override
    public CommonResult<Object> handleEnableApproveRecord(Object[] args) {
        SysDictDTO dictDTO = (SysDictDTO)args[0];
        SysDictData sysDictData = sysDictDataMapper.selectById(dictDTO.getId());

        if (dictDTO.getDisable()) {
            if (DictType.ScaleType.getCode().equals(sysDictData.getDictType())) {
                List<Long> gaugeIds = taskFeign.getGaugeIdByQuery(null, sysDictData.getDictValue(), 0).getData();
                if (CollectionUtils.isNotEmpty(gaugeIds)) {
                    throw exception("量表类型:" + sysDictData.getDictName() + " 已被使用，不能禁用");
                }
            }
        }

        String beforeJson = JsonUtils.writeValueAsStringExcludeNull(sysDictData);
        SysDictData afterData = new SysDictData();
        BeanUtil.copyProperties(sysDictData, afterData);
        afterData.setDictName(dictDTO.getDictName());
        afterData.setDisable(dictDTO.getDisable());
        String afterJson = JsonUtils.writeValueAsStringExcludeNull(afterData);
        ApproveOperation operation;
        if (dictDTO.getDisable()) {
            operation = ApproveOperation.DISABLE;
        } else {
            operation = ApproveOperation.ENABLE;
        }
        //添加审核记录
        approveService.addApproveRecord(ApproveType.DEPARTMENT, operation, ApproveStatus.TO_BE_APPROVED, dictDTO.getId(), SysUserUtil.getHeaderUserId(), beforeJson, afterJson);
        return CommonResult.success();
    }

    @Override
    public void beforeUpdateValid(Object[] args) {
        SysDictDTO dictDTO = (SysDictDTO)args[0];
        sysDictService.beforeUpdateValid(dictDTO);
    }
}
