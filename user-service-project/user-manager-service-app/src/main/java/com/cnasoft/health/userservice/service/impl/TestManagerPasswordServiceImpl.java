package com.cnasoft.health.userservice.service.impl;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.SysUserDTO;
import com.cnasoft.health.common.dto.TestManagerPasswordDTO;
import com.cnasoft.health.common.encryptor.EncryptorUtil;
import com.cnasoft.health.common.enums.ApplicationScene;
import com.cnasoft.health.common.enums.RoleEnum;
import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.convert.TestManagerPasswordConvert;
import com.cnasoft.health.userservice.feign.dto.TestManagerPasswordReqVO;
import com.cnasoft.health.userservice.mapper.TestManagerPasswordMapper;
import com.cnasoft.health.userservice.model.TestManagerPassword;
import com.cnasoft.health.userservice.service.ITestManagerPasswordService;
import com.cnasoft.health.userservice.util.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.cnasoft.health.common.exception.util.ServiceExceptionUtil.exception;
import static com.cnasoft.health.userservice.constant.UserErrorCodeConstants.OPERATION_NOT_ALLOWED;
import static com.cnasoft.health.userservice.constant.UserErrorCodeConstants.PASSWORD_NOT_SAME;
import static com.cnasoft.health.userservice.constant.UserErrorCodeConstants.SCENE_EXISTS_PASSWORD;
import static com.cnasoft.health.userservice.constant.UserErrorCodeConstants.UNKNOWN_SCENE;

/**
 * 测试管理员密码管理
 *
 * @author ganghe
 * @date 2022/7/15 10:18
 **/
@Slf4j
@Service
public class TestManagerPasswordServiceImpl extends SuperServiceImpl<TestManagerPasswordMapper, TestManagerPassword> implements ITestManagerPasswordService {
    @Value("${user.password.key}")
    private String key;

    @Override
    public PageResult<TestManagerPasswordDTO> findListByPage(Map<String, Object> params) {
        int pageNum = MapUtil.getInt(params, Constant.PAGE_NUM, 1);
        int pageSize = MapUtil.getInt(params, Constant.PAGE_SIZE, 10);
        Page<TestManagerPasswordDTO> page = new Page<>(pageNum, pageSize);
        params.put("key", key);
        params.put("userId", UserUtil.getUserId());

        List<TestManagerPasswordDTO> listByPage = baseMapper.findListByPage(page, params);
        long total = page.getTotal();

        return PageResult.<TestManagerPasswordDTO>builder().data(listByPage).count(total).build();
    }

    @Override
    public Boolean savePassword(TestManagerPasswordReqVO reqVO) {
        TestManagerPassword tmPassword = TestManagerPasswordConvert.INSTANCE.convert(reqVO);
        Long userId = UserUtil.getUserId();
        tmPassword.setUserId(userId);

        String password = tmPassword.getPassword();
        password = EncryptorUtil.decrypt(password);
        String confirmPassword = reqVO.getConfirmPassword();
        confirmPassword = EncryptorUtil.decrypt(confirmPassword);
        if (!password.equals(confirmPassword)) {
            throw exception(PASSWORD_NOT_SAME);
        }

        ApplicationScene applicationScene = ApplicationScene.getApplicationScene(tmPassword.getApplicationScene());
        if (applicationScene.equals(ApplicationScene.UNKNOWN)) {
            throw exception(UNKNOWN_SCENE);
        }

        // 判断该学校下该场景是否已添加密码
        Long existId = baseMapper.findExist(tmPassword.getUserId(), tmPassword.getSchoolId(), tmPassword.getApplicationScene());
        if (Objects.nonNull(existId) && existId > 0) {
            throw exception(SCENE_EXISTS_PASSWORD);
        }

        Date now = new Date();
        tmPassword.setPassword(password);
        tmPassword.setKey(key);
        tmPassword.setCreateBy(userId);
        tmPassword.setCreateTime(now);
        tmPassword.setUpdateBy(userId);
        tmPassword.setUpdateTime(now);
        tmPassword.setIsDeleted(false);

        int count = baseMapper.insert(tmPassword);
        return count > 0 ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override
    public Boolean updatePassword(TestManagerPasswordReqVO reqVO) {
        TestManagerPassword tmPassword = TestManagerPasswordConvert.INSTANCE.convert(reqVO);
        Long userId = UserUtil.getUserId();
        tmPassword.setUserId(userId);

        ApplicationScene applicationScene = ApplicationScene.getApplicationScene(tmPassword.getApplicationScene());
        if (ApplicationScene.UNKNOWN.equals(applicationScene)) {
            throw exception(UNKNOWN_SCENE);
        }

        // 判断该学校下该场景是否已添加密码
        Long existId = baseMapper.findExist(userId, tmPassword.getSchoolId(), tmPassword.getApplicationScene());
        if (Objects.nonNull(existId) && !existId.equals(tmPassword.getId())) {
            throw exception(SCENE_EXISTS_PASSWORD);
        }

        tmPassword.setKey(key);
        tmPassword.setUpdateBy(userId);
        tmPassword.setUpdateTime(new Date());

        String password = tmPassword.getPassword();
        tmPassword.setPassword(EncryptorUtil.decrypt(password));

        int count = baseMapper.update(tmPassword);
        return count > 0 ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override
    public List<BatchOperationTipDTO> deletePassword(Set<Long> ids) {
        List<BatchOperationTipDTO> resultMap = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(ids)) {
            for (Long id : ids) {
                TestManagerPassword password = baseMapper.selectById(id);
                if (Objects.isNull(password)) {
                    resultMap.add(new BatchOperationTipDTO(id, "无效id"));
                    continue;
                }

                baseMapper.delete(new LambdaQueryWrapper<TestManagerPassword>().eq(TestManagerPassword::getId, id));
            }
        }
        return resultMap;
    }

    @Override
    public Boolean checkPassword(TestManagerPasswordReqVO reqVO) {
        SysUserDTO user = UserUtil.getCurrentUser();
        String roleCode = user.getPresetRoleCode();
        Assert.isTrue(RoleEnum.student.getValue().equals(roleCode), OPERATION_NOT_ALLOWED.getMessage());

        Integer applicationSceneCode = reqVO.getCheckApplicationScene();
        ApplicationScene applicationScene = ApplicationScene.getApplicationScene(applicationSceneCode);
        if (ApplicationScene.UNKNOWN.equals(applicationScene)) {
            throw exception(UNKNOWN_SCENE);
        }

        String password = baseMapper.findPassword(reqVO.getTestManagerUserId(), user.getSchoolId(), applicationSceneCode, key);
        if (StringUtils.isBlank(password)) {
            password = ApplicationScene.getPassword(applicationSceneCode);
        }

        if (password.equals(reqVO.getCheckPassword())) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    @Override
    public Boolean checkPasswordWithLogOut(TestManagerPasswordReqVO reqVO) {
        String password = ApplicationScene.getPassword(ApplicationScene.LOG_OUT.getCode());
        if (password.equals(reqVO.getCheckPassword())) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }
}
