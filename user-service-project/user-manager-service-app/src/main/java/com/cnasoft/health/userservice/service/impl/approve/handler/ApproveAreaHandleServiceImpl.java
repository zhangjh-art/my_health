package com.cnasoft.health.userservice.service.impl.approve.handler;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import com.cnasoft.health.common.annotation.approve.ApproveBeanName;
import com.cnasoft.health.common.dto.SysAreaDTO;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.util.JsonUtils;
import com.cnasoft.health.userservice.feign.dto.ApproveVO;
import com.cnasoft.health.userservice.mapper.SysAreaMapper;
import com.cnasoft.health.userservice.model.Approve;
import com.cnasoft.health.userservice.model.SysArea;
import com.cnasoft.health.userservice.service.ISysAreaService;
import com.google.common.collect.Sets;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.cnasoft.health.common.exception.constant.GlobalErrorCodeConstants.BAD_REQUEST;

/**
 * @author Administrator
 */
@Service(ApproveBeanName.APPROVE_AREA_HANDLER)
public class ApproveAreaHandleServiceImpl implements ApproveHandleService {

    @Resource
    SysAreaMapper areaMapper;
    @Resource
    ISysAreaService areaService;

    @Override
    public void handleAddApprove(Approve approve, boolean allow) {
        SysArea area = areaMapper.selectById(approve.getBusinessId());
        Assert.notNull(area, BAD_REQUEST.getMessage());
        area.setApproveStatus(allow ? ApproveStatus.APPROVED.getCode() : ApproveStatus.REJECTED.getCode());
        areaService.updateArea(area);
    }

    @Override
    public void handleDeleteApprove(Approve approve) {
        areaService.delete(Sets.newHashSet(approve.getBusinessId()));
    }

    @Override
    public void handleUpdateApprove(Approve approve) {
        SysArea area = JsonUtils.readValue(approve.getAfterString(), SysArea.class);
        assert area != null;
        Assert.notNull(area, BAD_REQUEST.getMessage());
        areaService.updateArea(area);
    }

    @Override
    public void handleEnableApprove(Approve approve) {
        SysArea area = JsonUtils.readValue(approve.getAfterString(), SysArea.class);
        assert area != null;
        Assert.notNull(area, BAD_REQUEST.getMessage());
        areaService.updateEnabled(area.getId(), area.getEnabled());
    }

    @Override
    public List<Long> queryApproveBusinessId(Map<String, Object> params) {
        String query = MapUtil.getStr(params, "query");
        if (StringUtils.isEmpty(query)) {
            return null;
        }
        List<Long> result = areaMapper.getApproveAreaId(query);
        return result == null ? new ArrayList<>() : result;
    }

    @Override
    public void handleQueryResult(ApproveVO approve) {
        approve.setBeforeJson(handleQueryResult(approve.getBeforeJson()));
        approve.setAfterJson(handleQueryResult(approve.getAfterJson()));
    }

    private String handleQueryResult(String json) {
        if (StringUtils.isEmpty(json)) {
            return json;
        }
        SysArea area = JsonUtils.readValue(json, SysArea.class);
        assert area != null;
        Assert.notNull(area, BAD_REQUEST.getMessage());
        SysAreaDTO sysAreaDTO = new SysAreaDTO();
        BeanUtils.copyProperties(area, sysAreaDTO);
        areaService.getAreaNameInfo(sysAreaDTO);
        return JsonUtils.writeValueAsString(sysAreaDTO);
    }
}
