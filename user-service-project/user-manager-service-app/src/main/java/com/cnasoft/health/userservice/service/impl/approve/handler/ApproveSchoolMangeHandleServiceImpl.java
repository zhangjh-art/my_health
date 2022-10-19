package com.cnasoft.health.userservice.service.impl.approve.handler;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cnasoft.health.common.annotation.approve.ApproveBeanName;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.util.JsonUtils;
import com.cnasoft.health.userservice.feign.dto.SchoolManagerUpdateReqVO;
import com.cnasoft.health.userservice.mapper.SchoolManagerMapper;
import com.cnasoft.health.userservice.model.Approve;
import com.cnasoft.health.userservice.model.SchoolManager;
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
@Service(ApproveBeanName.APPROVE_SCHOOL_MANAGER_HANDLER)
public class ApproveSchoolMangeHandleServiceImpl implements ApproveHandleService {
    @Resource
    private ISysUserService sysUserService;
    @Resource
    private SchoolManagerMapper schoolManagerMapper;

    @Override
    public void handleAddApprove(Approve approve, boolean allow) throws Exception {
        LambdaQueryWrapper<SchoolManager> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SchoolManager::getUserId, approve.getBusinessId());
        SchoolManager manager = schoolManagerMapper.selectOne(queryWrapper);
        Assert.notNull(manager, BAD_REQUEST.getMessage());
        manager.setApproveStatus(allow ? ApproveStatus.APPROVED.getCode() : ApproveStatus.REJECTED.getCode());
        schoolManagerMapper.updateById(manager);

        SysUser sysUser = new SysUser();
        sysUser.setId(manager.getUserId());
        sysUser.setApproveStatus(allow ? ApproveStatus.APPROVED.getCode() : ApproveStatus.REJECTED.getCode());
        sysUserService.updateUserPublic(sysUser);
        sysUserService.cacheUser(manager.getUserId());
    }

    @Override
    public void handleDeleteApprove(Approve approve) {
        sysUserService.delUser(Sets.newHashSet(approve.getBusinessId()));
    }

    @Override
    public void handleUpdateApprove(Approve approve) throws Exception {
        SchoolManagerUpdateReqVO vo = JsonUtils.readValue(approve.getAfterString(), SchoolManagerUpdateReqVO.class);
        assert vo != null;
        Assert.notNull(vo, BAD_REQUEST.getMessage());
        sysUserService.updateSchoolManager(vo);
    }

    @Override
    public void handleEnableApprove(Approve approve) {
        SchoolManagerUpdateReqVO user = JsonUtils.readValue(approve.getAfterString(), SchoolManagerUpdateReqVO.class);
        assert user != null;
        Assert.notNull(user, BAD_REQUEST.getMessage());
        sysUserService.updateEnabled(user.getId(), user.getEnabled());
    }

    @Override
    public List<Long> queryApproveBusinessId(Map<String, Object> params) {
        String query = MapUtil.getStr(params, "query");
        if (StringUtils.isEmpty(query)) {
            return null;
        }
        List<Long> result = sysUserService.getSchoolManagerIdByQuery(query);
        return result == null ? new ArrayList<>() : result;
    }

}
