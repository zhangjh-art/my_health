package com.cnasoft.health.userservice.service.impl.approve.handler;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import com.cnasoft.health.common.annotation.approve.ApproveBeanName;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.util.JsonUtils;
import com.cnasoft.health.userservice.feign.dto.AreaManagerUpdateReqVO;
import com.cnasoft.health.userservice.model.Approve;
import com.cnasoft.health.userservice.model.SysUser;
import com.cnasoft.health.userservice.service.ISysUserService;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.cnasoft.health.common.exception.constant.GlobalErrorCodeConstants.BAD_REQUEST;

/**
 * @author Administrator
 */
@Service(ApproveBeanName.APPROVE_AREA_MANAGER_HANDLER)
public class ApproveAreaManagerHandleServiceImpl implements ApproveHandleService {

    @Resource
    private ISysUserService sysUserService;

    @Override
    public void handleAddApprove(Approve approve, boolean allow) throws Exception {
        SysUser areaManager = sysUserService.selectSysUserById(approve.getBusinessId());
        Assert.notNull(areaManager, BAD_REQUEST.getMessage());
        areaManager.setApproveStatus(allow ? ApproveStatus.APPROVED.getCode() : ApproveStatus.REJECTED.getCode());
        sysUserService.updateUserPublic(areaManager);
        sysUserService.cacheUser(areaManager.getId());
    }

    @Override
    public void handleDeleteApprove(Approve approve) {
        sysUserService.delUser(Sets.newHashSet(approve.getBusinessId()));
    }

    @Override
    public void handleUpdateApprove(Approve approve) throws Exception {
        SysUser areaManager = sysUserService.selectSysUserById(approve.getBusinessId());
        Assert.notNull(areaManager, BAD_REQUEST.getMessage());
        AreaManagerUpdateReqVO after = JsonUtils.readValue(approve.getAfterString(), AreaManagerUpdateReqVO.class);
        assert after != null;
        Assert.notNull(after, BAD_REQUEST.getMessage());
        areaManager.setAreaCode(after.getAreaCode());
        areaManager.setName(after.getName());
        areaManager.setSex(after.getSex());
        areaManager.setMobile(after.getMobile());
        areaManager.setEmail(after.getEmail());
        areaManager.setEnabled(after.getEnabled());
        areaManager.setApproveStatus(ApproveStatus.APPROVED.getCode());

        sysUserService.updateUserPublic(areaManager);
        sysUserService.cacheUser(areaManager.getId());
    }

    @Override
    public void handleEnableApprove(Approve approve) {
        SysUser user = JsonUtils.readValue(approve.getAfterString(), SysUser.class);
        assert user != null;
        Assert.notNull(user, BAD_REQUEST.getMessage());
        sysUserService.updateEnabled(user.getId(), user.getEnabled());
    }

    @Override
    public List<Long> queryApproveBusinessId(Map<String, Object> params) {
        String query = MapUtil.getStr(params, "query");
        Integer areaType = MapUtil.getInt(params, "areaType", -1);
        if (StringUtils.isEmpty(query) && areaType == -1) {
            return null;
        }
        List<Long> result = sysUserService.getAreaManagerIdByQuery(query, areaType);
        return result == null ? new ArrayList<>() : result;
    }
}
