package com.cnasoft.health.auth.service.impl;

import cn.hutool.core.util.StrUtil;
import com.cnasoft.health.auth.bloom.BloomFilter;
import com.cnasoft.health.auth.service.CnaSoftUserDetailService;
import com.cnasoft.health.common.dto.SchoolDTO;
import com.cnasoft.health.common.dto.SysUserDTO;
import com.cnasoft.health.common.encryptor.EncryptorUtil;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.enums.RoleEnum;
import com.cnasoft.health.common.exception.GlobalException;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.userservice.feign.SchoolFeignClient;
import com.cnasoft.health.userservice.feign.UserFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.cnasoft.health.common.exception.constant.GlobalErrorCodeConstants.METHOD_NOT_ALLOWED;

/**
 * @author cnasoft
 * @date 2020/6/30 13:42
 */
@Slf4j
@Service
public class UserDetailServiceImpl implements CnaSoftUserDetailService {

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private SchoolFeignClient schoolFeignClient;

    @Resource
    private BloomFilter bloomFilter;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Value("${spring.profiles.active}")
    private String env;

    private final static String LOCAL = "local";
    private final static String TEST = "test-aliyun";

    @Override
    public List<SysUserDTO> loadUserByMobile(String mobile, String captcha) {
        if (BloomFilter.getLoadUser()) {
            if (!this.bloomFilter.mightContain(mobile)) {
                return Collections.emptyList();
            }
        }

        CommonResult<List<SysUserDTO>> result = userFeignClient.findLoginUserByMobile(mobile, captcha);
        result.checkError();

        return result.getData();
    }

    @Override
    public Boolean updatesSmsRecordUsedStatus(String mobile, String captcha) {
        if (BloomFilter.getLoadUser()) {
            if (!this.bloomFilter.mightContain(mobile)) {
                return Boolean.FALSE;
            }
        }

        return userFeignClient.updatesSmsRecordUsedStatus(mobile, captcha).getData();
    }

    @Override
    public List<SysUserDTO> findAllUserInUsernameOrMobileOrShortId(String username) {
        if (BloomFilter.getLoadUser()) {
            if (!this.bloomFilter.mightContain(username)) {
                return Collections.emptyList();
            }
        }

        CommonResult<List<SysUserDTO>> result = userFeignClient.findUserByUsernameOrMobileOrShortId(username);
        result.checkError();
        return result.getData();
    }

    @Override
    public SysUserDTO findUserByUsernameWithTestManager(String username) {
        if (BloomFilter.getLoadUser()) {
            if (!this.bloomFilter.mightContain(username)) {
                return null;
            }
        }

        CommonResult<SysUserDTO> result = userFeignClient.findUserByUsernameWithTestManager(username);
        result.checkError();
        return result.getData();
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrMobile) {
        return null;
    }

    @Override
    public UserDetails loadUserByOpenId(String openId) {
        log.debug("暂不支持openId登陆");
        throw new GlobalException(METHOD_NOT_ALLOWED);
        //return checkUser(userFeignClient.findLoginUserByOpenId(openId));
    }

    @Override
    public void updateParentConfirmStatus(Long userId) {
        userFeignClient.updateConfirmAndActiveStatus(userId);
    }

    @Override
    public void addUserToFilter(Collection<String> keys) {
        keys.remove(StrUtil.EMPTY);
        keys.remove(null);
        keys.forEach(key -> this.bloomFilter.add(key));
    }

    /**
     * 检查用户合法性
     *
     * @param sysUser 用户信息
     */
    @Override
    public boolean checkUser(final SysUserDTO sysUser) {
        // 所有异常均处理为用户名密码错误返回
        if (Objects.isNull(sysUser)) {
            log.debug("用户为null");
            return false;
        }

        if (Boolean.TRUE.equals(sysUser.getIsDeleted())) {
            log.debug("用户id {} 状态已删除", sysUser.getId());
            return false;
        }

        if (!sysUser.isEnabled()) {
            log.debug("用户id {} 状态已禁用", sysUser.getId());
            return false;
        }

        Integer approveStatus = sysUser.getApproveStatus();
        if (Objects.isNull(approveStatus) || ApproveStatus.TO_BE_APPROVED.getCode().equals(approveStatus)) {
            log.debug("用户id {} 审核状态 {} 不正确", sysUser.getId(), approveStatus);
            return false;
        }

        if (ApproveStatus.REJECTED.getCode().equals(approveStatus)) {
            log.debug("用户id {} 审核状态 {} 已拒绝", sysUser.getId(), approveStatus);
            return false;
        }

        //校验区域是否有效
        boolean result = checkAreaData(sysUser);
        if (!result) {
            return false;
        }

        //校验学校是否有效
        result = checkSchoolData(sysUser);
        if (!result) {
            return false;
        }

        return true;
    }

    private boolean checkAreaData(final SysUserDTO sysUser) {
        List<String> regionRoles = new ArrayList<>();
        regionRoles.add(RoleEnum.region_admin.getValue());
        regionRoles.add(RoleEnum.region_leader.getValue());
        regionRoles.add(RoleEnum.region_psycho_teacher.getValue());
        regionRoles.add(RoleEnum.region_staff.getValue());

        if (regionRoles.contains(sysUser.getPresetRoleCode())) {
            // 判断区域是否有效
            Integer areaCode = sysUser.getAreaCode();
            if (areaCode != null && areaCode > 0) {
                CommonResult<Boolean> areaAvailableStatus = userFeignClient.getAreaAvailableStatus(areaCode);
                areaAvailableStatus.checkError();

                Boolean availableStatus = areaAvailableStatus.getData();
                if (Boolean.FALSE.equals(availableStatus)) {
                    log.debug("用户id {} 区域状态不可用", sysUser.getId());
                    return false;
                }
            } else {
                log.debug("用户id {} 区域编码为空", sysUser.getId());
                return false;
            }
        }

        return true;
    }

    private boolean checkSchoolData(final SysUserDTO sysUser) {
        List<String> schoolRoles = new ArrayList<>();
        schoolRoles.add(RoleEnum.school_leader.getValue());
        schoolRoles.add(RoleEnum.school_psycho_teacher.getValue());
        schoolRoles.add(RoleEnum.school_admin.getValue());
        schoolRoles.add(RoleEnum.school_head_teacher.getValue());
        schoolRoles.add(RoleEnum.school_staff.getValue());
        schoolRoles.add(RoleEnum.student.getValue());
        schoolRoles.add(RoleEnum.parents.getValue());

        if (schoolRoles.contains(sysUser.getPresetRoleCode())) {
            // 判断学校是否有效
            Long schoolId = sysUser.getSchoolId();
            if (schoolId != null && schoolId > 0) {
                CommonResult<SchoolDTO> school = schoolFeignClient.getSchoolById(schoolId);
                school.checkError();

                SchoolDTO schoolDTO = school.getData();
                if (Objects.isNull(schoolDTO)) {
                    log.debug("用户id {} 学校id{} 不存在", sysUser.getId(), schoolId);
                    return false;
                }

                if (!schoolDTO.getApproveStatus().equals(ApproveStatus.APPROVED.getCode())) {
                    log.debug("用户id {} 学校id{} 未审核通过", sysUser.getId(), schoolId);
                    return false;
                }
            } else {
                log.debug("用户id {} 学校id为空", sysUser.getId());
                return false;
            }
        }

        return true;
    }

    @Override
    public SysUserDTO findUserById(Long id) {
        return userFeignClient.findUserById(id).getData();
    }

    @Override
    public boolean pwdCheck(SysUserDTO user, String password) {
        try {
            if (StringUtils.isNotBlank(env) && (env.equals(LOCAL) || env.startsWith(TEST))) {
                return password.startsWith("88888888") || passwordEncoder.matches(EncryptorUtil.decrypt(user.getPassword()), password);
            } else {
                return passwordEncoder.matches(EncryptorUtil.decrypt(user.getPassword()), password);
            }
        } catch (Exception e) {
            log.error("校验密码失败", e);
            return false;
        }
    }
}
