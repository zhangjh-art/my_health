package com.cnasoft.health.userservice.service.impl.approve.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cnasoft.health.common.annotation.approve.ApproveBeanName;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.util.JsonUtils;
import com.cnasoft.health.userservice.feign.dto.TestManagerReqVO;
import com.cnasoft.health.userservice.mapper.SysUserMapper;
import com.cnasoft.health.userservice.mapper.TestManagerMapper;
import com.cnasoft.health.userservice.model.Approve;
import com.cnasoft.health.userservice.model.SysUser;
import com.cnasoft.health.userservice.model.TestManager;
import com.cnasoft.health.userservice.service.ISysUserService;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.cnasoft.health.common.exception.constant.GlobalErrorCodeConstants.BAD_REQUEST;

/**
 * 测试管理员审核操作类
 *
 * @author ganghe
 */
@Service(ApproveBeanName.APPROVE_TEST_MANAGER_HANDLER)
public class ApproveTestMangeHandleServiceImpl implements ApproveHandleService {
    @Value("${user.password.key}")
    private String key;
    @Resource
    private ISysUserService sysUserService;
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private TestManagerMapper testManagerMapper;

    @Override
    public void handleAddApprove(Approve approve, boolean allow) throws Exception {
        SysUser sysUser = sysUserMapper.selectOneById(key, approve.getBusinessId());
        Assert.notNull(sysUser, BAD_REQUEST.getMessage());

        Long userId = sysUser.getId();
        sysUser.setApproveStatus(allow ? ApproveStatus.APPROVED.getCode() : ApproveStatus.REJECTED.getCode());
        sysUserService.updateUserPublic(sysUser);
        sysUserService.cacheUser(userId);

        List<TestManager> testManagers = testManagerMapper.selectList(new LambdaQueryWrapper<TestManager>().eq(TestManager::getUserId, userId));
        if (CollUtil.isNotEmpty(testManagers)) {
            for (TestManager testManager : testManagers) {
                testManager.setApproveStatus(allow ? ApproveStatus.APPROVED.getCode() : ApproveStatus.REJECTED.getCode());
            }

            testManagerMapper.updateBatch(testManagers);
        }
    }

    @Override
    public void handleUpdateApprove(Approve approve) throws Exception {
        TestManagerReqVO vo = JsonUtils.readValue(approve.getAfterString(), TestManagerReqVO.class);
        assert vo != null;
        Assert.notNull(vo, BAD_REQUEST.getMessage());
        sysUserService.updateTestManager(vo);
    }

    @Override
    public void handleDeleteApprove(Approve approve) {
        sysUserService.delUser(Sets.newHashSet(approve.getBusinessId()));
    }

    @Override
    public void handleEnableApprove(Approve approve) {
        TestManagerReqVO user = JsonUtils.readValue(approve.getAfterString(), TestManagerReqVO.class);
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
        List<Long> result = sysUserService.getTestManagerIdByQuery(query);
        return result == null ? new ArrayList<>() : result;
    }
}
