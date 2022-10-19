package com.cnasoft.health.userservice.service.impl.approve.handler;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cnasoft.health.common.annotation.approve.ApproveBeanName;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.util.JsonUtils;
import com.cnasoft.health.userservice.convert.SysDictConvert;
import com.cnasoft.health.userservice.mapper.SysDictDataMapper;
import com.cnasoft.health.userservice.model.Approve;
import com.cnasoft.health.userservice.model.SysDictData;
import com.cnasoft.health.userservice.service.ISysDictService;
import com.cnasoft.health.userservice.util.UserUtil;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.cnasoft.health.common.exception.constant.GlobalErrorCodeConstants.BAD_REQUEST;

/**
 * @author Administrator
 */
@Service(ApproveBeanName.APPROVE_DICT_DATA_HANDLER)
public class ApproveDictDataHandleServiceImpl implements ApproveHandleService {
    @Resource
    private SysDictDataMapper sysDictDataMapper;
    @Resource
    private ISysDictService sysDictService;

    @Override
    public void handleAddApprove(Approve approve, boolean allow) {
        SysDictData sysDictData = sysDictDataMapper.selectById(approve.getBusinessId());
        Assert.notNull(sysDictData, BAD_REQUEST.getMessage());

        SysDictData updateData = new SysDictData();
        updateData.setId(sysDictData.getId());
        updateData.setApproveStatus(allow ? ApproveStatus.APPROVED.getCode() : ApproveStatus.REJECTED.getCode());
        sysDictService.updateDictData(SysDictConvert.INSTANCE.convertDictDTO(updateData));
    }

    @Override
    public void handleDeleteApprove(Approve approve) {
        sysDictService.deleteDictData(Sets.newHashSet(approve.getBusinessId()));
    }

    @Override
    public void handleUpdateApprove(Approve approve) {
        SysDictData sysDictData = JsonUtils.readValue(approve.getAfterString(), SysDictData.class);
        Assert.notNull(sysDictData, BAD_REQUEST.getMessage());

        if (!ApproveStatus.APPROVED.getCode().equals(sysDictData.getApproveStatus())) {
            sysDictData.setApproveStatus(ApproveStatus.APPROVED.getCode());
        }

        sysDictService.updateDictData(SysDictConvert.INSTANCE.convertDictDTO(sysDictData));
    }

    @Override
    public List<Long> queryApproveBusinessId(Map<String, Object> params) {
        String dictName = MapUtil.getStr(params, "dictName");
        String dictType = MapUtil.getStr(params, "dictType");
        if (StringUtils.isEmpty(dictName) && StringUtils.isEmpty(dictType)) {
            return null;
        }
        List<Long> result = sysDictDataMapper.getDictDateIdByQuery(dictName, dictType);
        return result == null ? new ArrayList<>() : result;
    }

    @Override
    public void handleEnableApprove(Approve approve) {
        SysDictData sysDictData = JsonUtils.readValue(approve.getAfterString(), SysDictData.class);
        Assert.notNull(sysDictData, BAD_REQUEST.getMessage());

        LambdaUpdateWrapper<SysDictData> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysDictData::getId, sysDictData.getId());
        updateWrapper.set(SysDictData::getDisable, sysDictData.getDisable());
        updateWrapper.set(SysDictData::getUpdateBy, UserUtil.getUserId());
        updateWrapper.set(SysDictData::getUpdateTime, new Date());
        sysDictDataMapper.update(null, updateWrapper);
        sysDictService.cacheDictData(sysDictData.getId());
    }
}
