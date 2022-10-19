package com.cnasoft.health.userservice.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.constant.CommonConstant;
import com.cnasoft.health.common.constant.RedisConstant;
import com.cnasoft.health.common.constant.RocketMQConstant;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.CommonDTO;
import com.cnasoft.health.common.dto.MessageDTO;
import com.cnasoft.health.common.dto.RegionStaffInfoDTO;
import com.cnasoft.health.common.dto.SchoolDTO;
import com.cnasoft.health.common.dto.StudentBaseInfoDTO;
import com.cnasoft.health.common.dto.SysAreaDTO;
import com.cnasoft.health.common.dto.SysAuthoritySimpleDTO;
import com.cnasoft.health.common.dto.SysUserDTO;
import com.cnasoft.health.common.dto.TransactionMsgDefinationDTO;
import com.cnasoft.health.common.encryptor.DesensitizedUtil;
import com.cnasoft.health.common.encryptor.EncryptorUtil;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.enums.RoleEnum;
import com.cnasoft.health.common.enums.Sex;
import com.cnasoft.health.common.exception.IdempotencyException;
import com.cnasoft.health.common.exception.LockException;
import com.cnasoft.health.common.lock.IDistLock;
import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.common.util.JsonUtils;
import com.cnasoft.health.common.util.SysUserUtil;
import com.cnasoft.health.common.util.text.TextValidator;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.evaluation.feign.WarningFeign;
import com.cnasoft.health.fileapi.fegin.FileFeignClient;
import com.cnasoft.health.fileapi.fegin.dto.FileFeignUploadDTO;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.constant.UserConstant;
import com.cnasoft.health.userservice.convert.SysAuthorityConvert;
import com.cnasoft.health.userservice.convert.SysUserConvert;
import com.cnasoft.health.userservice.enums.ServiceObject;
import com.cnasoft.health.userservice.feign.AuthFeign;
import com.cnasoft.health.userservice.feign.TaskFeign;
import com.cnasoft.health.userservice.feign.dto.AreaManagerCreateReqVO;
import com.cnasoft.health.userservice.feign.dto.AreaManagerUpdateReqVO;
import com.cnasoft.health.userservice.feign.dto.ForgetPasswordVO;
import com.cnasoft.health.userservice.feign.dto.SchoolManagerCreateReqVO;
import com.cnasoft.health.userservice.feign.dto.SchoolManagerUpdateReqVO;
import com.cnasoft.health.userservice.feign.dto.SysUserAuthorityReqVO;
import com.cnasoft.health.userservice.feign.dto.SysUserCreateReqVO;
import com.cnasoft.health.userservice.feign.dto.SysUserReqVO;
import com.cnasoft.health.userservice.feign.dto.SysUserUpdateMobileReqVO;
import com.cnasoft.health.userservice.feign.dto.SysUserUpdateReqVO;
import com.cnasoft.health.userservice.feign.dto.TaskUserReqVO;
import com.cnasoft.health.userservice.feign.dto.TaskUserResVO;
import com.cnasoft.health.userservice.feign.dto.TestManagerReqVO;
import com.cnasoft.health.userservice.feign.dto.UpdatePasswordReqVO;
import com.cnasoft.health.userservice.feign.dto.UpgradeDTO;
import com.cnasoft.health.userservice.mapper.AreaStaffMapper;
import com.cnasoft.health.userservice.mapper.AreaTeacherMapper;
import com.cnasoft.health.userservice.mapper.ConsultationReportMapper;
import com.cnasoft.health.userservice.mapper.MessageMapper;
import com.cnasoft.health.userservice.mapper.NewReservationMapper;
import com.cnasoft.health.userservice.mapper.ParentMapper;
import com.cnasoft.health.userservice.mapper.RocketmqTransactionLogMapper;
import com.cnasoft.health.userservice.mapper.SchoolManagerMapper;
import com.cnasoft.health.userservice.mapper.SchoolStaffMapper;
import com.cnasoft.health.userservice.mapper.SchoolTeacherMapper;
import com.cnasoft.health.userservice.mapper.SmsRecordMapper;
import com.cnasoft.health.userservice.mapper.StudentBaseInfoMapper;
import com.cnasoft.health.userservice.mapper.SysUserAuthorityMapper;
import com.cnasoft.health.userservice.mapper.SysUserMapper;
import com.cnasoft.health.userservice.mapper.SysUserRoleMapper;
import com.cnasoft.health.userservice.mapper.TestManagerMapper;
import com.cnasoft.health.userservice.mapper.TestManagerPasswordMapper;
import com.cnasoft.health.userservice.mapper.UserDynamicMapper;
import com.cnasoft.health.userservice.model.AreaTeacher;
import com.cnasoft.health.userservice.model.ConsultationReport;
import com.cnasoft.health.userservice.model.Message;
import com.cnasoft.health.userservice.model.NewReservation;
import com.cnasoft.health.userservice.model.Parent;
import com.cnasoft.health.userservice.model.RocketmqTransactionLog;
import com.cnasoft.health.userservice.model.SchoolManager;
import com.cnasoft.health.userservice.model.SchoolStaff;
import com.cnasoft.health.userservice.model.SchoolTeacher;
import com.cnasoft.health.userservice.model.SmsRecord;
import com.cnasoft.health.userservice.model.StudentBaseInfo;
import com.cnasoft.health.userservice.model.SysAuthority;
import com.cnasoft.health.userservice.model.SysRole;
import com.cnasoft.health.userservice.model.SysUser;
import com.cnasoft.health.userservice.model.SysUserAuthority;
import com.cnasoft.health.userservice.model.SysUserRole;
import com.cnasoft.health.userservice.model.TestManager;
import com.cnasoft.health.userservice.platform.RemoteRestAPI;
import com.cnasoft.health.userservice.platform.UserInfo;
import com.cnasoft.health.userservice.service.IMessageService;
import com.cnasoft.health.userservice.service.ISysAuthorityService;
import com.cnasoft.health.userservice.service.ISysRoleService;
import com.cnasoft.health.userservice.service.ISysUserAuthorityService;
import com.cnasoft.health.userservice.service.ISysUserRoleService;
import com.cnasoft.health.userservice.service.ISysUserService;
import com.cnasoft.health.userservice.util.DataCacheUtil;
import com.cnasoft.health.userservice.util.RedisUtils;
import com.cnasoft.health.userservice.util.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.cnasoft.health.common.exception.util.ServiceExceptionUtil.exception;
import static com.cnasoft.health.userservice.constant.UserErrorCodeConstants.OPERATION_NOT_ALLOWED;
import static com.cnasoft.health.userservice.constant.UserErrorCodeConstants.USER_NOT_EXISTS;
import static com.cnasoft.health.userservice.constant.UserErrorCodeConstants.USER_NO_ID;
import static com.cnasoft.health.userservice.constant.UserErrorCodeConstants.USER_OLD_PASSWORD_ERROR;

/**
 * @author ganghe
 */
@Slf4j
@Service
public class SysUserServiceImpl extends SuperServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
    @Value("${user.password.key}")
    private String key;
    @Resource
    private ISysUserRoleService userRoleService;
    @Resource
    private SysUserRoleMapper userRoleMapper;
    @Resource
    private ISysRoleService roleService;
    @Resource
    private SysUserAuthorityMapper userAuthorityMapper;
    @Resource
    private ISysUserAuthorityService userAuthorityService;
    @Resource
    private IMessageService messageService;
    @Resource
    private IDistLock lock;
    @Resource
    private AreaStaffMapper areaStaffMapper;
    @Resource
    private ISysAuthorityService authorityService;
    @Resource
    private SchoolStaffMapper schoolStaffMapper;
    @Resource
    private StudentBaseInfoMapper studentMapper;
    @Resource
    private ParentMapper parentMapper;
    @Resource
    private SchoolManagerMapper schoolManagerMapper;
    @Resource
    private TestManagerMapper testManagerMapper;
    @Resource
    private TestManagerPasswordMapper testManagerPasswordMapper;
    @Resource
    private AreaTeacherMapper areaTeacherMapper;
    @Resource
    private SchoolTeacherMapper schoolTeacherMapper;
    @Resource
    private SmsRecordMapper smsRecordMapper;
    @Resource
    private AuthFeign authFeign;
    @Resource
    private TaskFeign taskFeign;
    @Resource
    private FileFeignClient fileFeign;
    @Resource
    WarningFeign warningFeign;
    @Resource
    private UserDynamicMapper userDynamicMapper;
    @Resource
    private NewReservationMapper reservationMapper;
    @Resource
    private MessageMapper messageMapper;
    @Resource
    private ConsultationReportMapper consultationReportMapper;
    @Resource
    RemoteRestAPI remoteRestAPI;
    @Resource
    private TaskExecutor taskExecutor;
    @Resource
    RocketMQTemplate rocketMQTemplate;
    @Resource
    RocketmqTransactionLogMapper rocketmqTransactionLogMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateUserBaseInfo(SysUserUpdateReqVO sysUserUpdateReqVO) throws Exception {
        updateUser(sysUserUpdateReqVO);
    }

    public void updateUser(SysUserUpdateReqVO sysUserUpdateReqVO) throws Exception {
        SysUser sysUser = SysUserConvert.INSTANCE.convertVO(sysUserUpdateReqVO);

        // 更新用户信息
        updateUserPublic(sysUser);

        cacheUser(sysUser.getId());
    }

    private void saveUserRole(Long userId, List<String> roleCodes) {
        if (!CollectionUtils.isEmpty(roleCodes)) {
            List<SysUserRole> userRoles = new ArrayList<>();
            Set<String> roleCodeSet = new HashSet<>(roleCodes);
            for (String roleCode : roleCodeSet) {
                SysRole role = roleService.getOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, roleCode));
                if (Objects.isNull(role)) {
                    continue;
                }
                userRoles.add(new SysUserRole(userId, role.getId(), roleCode));
            }
            userRoleService.saveBatch(userRoles);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveFirstLevelManager(SysUserCreateReqVO createReqVO) throws Exception {
        SysUser sysUser = SysUserConvert.INSTANCE.convertVO(createReqVO);

        // 保存用户账号数据
        return saveUserPublic(sysUser, RoleEnum.first_level_admin);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveSecondLevelManager(SysUserCreateReqVO createReqVO) throws Exception {
        SysUser sysUser = SysUserConvert.INSTANCE.convertVO(createReqVO);

        // 保存用户账号数据
        return saveUserPublic(sysUser, RoleEnum.second_level_admin);
    }

    @Override
    public List<Long> getSchoolManagerIdByQuery(String query) {
        return baseMapper.getSchoolManagerIdByQuery(query, key);
    }

    @Override
    public List<Long> getTestManagerIdByQuery(String query) {
        return baseMapper.getTestManagerIdByQuery(query, key);
    }

    @Override
    public Boolean checkExistsUserByUserIdAndSchoolId(Long userId, Long schoolId) {
        return baseMapper.checkExistsUserByUserIdAndSchoolId(userId, schoolId);
    }

    @Override
    public Boolean checkExistsUserByUserIdAndAreaCode(Long userId, Integer areaCode) {
        return baseMapper.checkExistsUserByUserIdAndAreaCode(userId, areaCode);
    }

    @Override
    public List<Long> getAreaManagerIdByQuery(String query, Integer areaType) {
        return baseMapper.getAreaManagerIdByQuery(query, key, areaType);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SchoolManager saveSchoolManager(SchoolManagerCreateReqVO managerCreateReqVO) throws Exception {
        SysUser sysUser = SysUserConvert.INSTANCE.convertVO(managerCreateReqVO);
        // 保存用户账号数据
        sysUser.setAreaCode(null);
        boolean result = saveUserPublic(sysUser, RoleEnum.school_admin);

        if (!result) {
            return null;
        }

        // 保存校级管理员数据
        SchoolManager manager = new SchoolManager();
        manager.setSchoolId(managerCreateReqVO.getSchoolId());
        manager.setUserId(sysUser.getId());
        schoolManagerMapper.insert(manager);

        return manager;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public TestManagerReqVO saveTestManager(TestManagerReqVO testManagerReqVO) throws Exception {
        SysUser sysUser = SysUserConvert.INSTANCE.convertVO(testManagerReqVO);
        sysUser.setApproveStatus(ApproveStatus.TO_BE_APPROVED.getCode());
        sysUser.setAreaCode(null);
        sysUser.setSchoolId(null);
        // 保存用户账号数据
        boolean result = saveUserPublic(sysUser, RoleEnum.test_admin);

        if (!result) {
            return testManagerReqVO;
        }

        Long userId = sysUser.getId();

        // 保存测试管理员数据
        List<SchoolDTO> schools = testManagerReqVO.getSchools();
        if (CollUtil.isNotEmpty(schools)) {
            saveTestManager(userId, schools, ApproveStatus.TO_BE_APPROVED.getCode());
        }

        testManagerReqVO.setId(userId);
        testManagerReqVO.setEnabled(true);
        return testManagerReqVO;
    }

    private void saveTestManager(Long userId, List<SchoolDTO> schools, Integer approveStatus) {
        List<TestManager> testManagers = new ArrayList<>();
        for (SchoolDTO school : schools) {
            TestManager testManager = new TestManager();
            testManager.setUserId(userId);
            testManager.setSchoolId(school.getId());
            testManager.setApproveStatus(approveStatus);
            testManagers.add(testManager);
        }
        testManagerMapper.insertBatch(testManagers);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateTestManager(TestManagerReqVO testManagerReqVO) throws Exception {
        SysUser sysUser = SysUserConvert.INSTANCE.convertVO(testManagerReqVO);
        sysUser.setApproveStatus(ApproveStatus.APPROVED.getCode());
        sysUser.setAreaCode(null);
        sysUser.setSchoolId(null);
        boolean result = updateUserPublic(sysUser);

        if (result) {
            Long userId = sysUser.getId();
            List<SchoolDTO> schools = testManagerReqVO.getSchools();
            if (CollUtil.isNotEmpty(schools)) {

                //查询之前的数据，对比是否与现在的数据一致
                List<SchoolDTO> beforeSchools = testManagerMapper.findSchoolList(userId, null);
                if (CollUtil.isNotEmpty(beforeSchools)) {
                    Set<Long> schoolIds = schools.stream().map(SchoolDTO::getId).collect(Collectors.toSet());
                    Set<Long> beforeSchoolIds = beforeSchools.stream().map(SchoolDTO::getId).collect(Collectors.toSet());
                    if (!schoolIds.equals(beforeSchoolIds)) {
                        //删除之前的数据
                        testManagerMapper.delete(new LambdaUpdateWrapper<TestManager>().eq(TestManager::getUserId, userId));

                        saveTestManager(userId, schools, ApproveStatus.APPROVED.getCode());
                    }
                } else {
                    saveTestManager(userId, schools, ApproveStatus.APPROVED.getCode());
                }
            }
            cacheUser(userId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateSchoolManager(SchoolManagerUpdateReqVO managerUpdateReqVO) throws Exception {
        SysUser sysUser = SysUserConvert.INSTANCE.convertVO(managerUpdateReqVO);
        sysUser.setApproveStatus(ApproveStatus.APPROVED.getCode());
        sysUser.setAreaCode(null);
        boolean result = updateUserPublic(sysUser);

        if (result && ObjectUtils.isNotEmpty(managerUpdateReqVO.getSchoolId())) {
            SchoolManager schoolManager = schoolManagerMapper.selectOne(new LambdaQueryWrapper<SchoolManager>().eq(SchoolManager::getUserId, sysUser.getId()));
            if (ObjectUtils.isNotEmpty(schoolManager) && !schoolManager.getSchoolId().equals(managerUpdateReqVO.getSchoolId())) {
                // 更新校级管理员数据
                schoolManager.setSchoolId(managerUpdateReqVO.getSchoolId());
                schoolManager.setApproveStatus(ApproveStatus.APPROVED.getCode());
                schoolManagerMapper.updateById(schoolManager);
            }
        }

        cacheUser(sysUser.getId());
    }

    @Override
    public void deleteSchoolManager(Set<Long> ids) {
        this.delUser(ids);
    }

    @Override
    public void deleteTestManager(Set<Long> ids) {
        this.delUser(ids);
    }

    @Override
    public void deleteSchoolManagerBySchool(Long schoolId) {
        LambdaQueryWrapper<SchoolManager> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SchoolManager::getSchoolId, schoolId);
        schoolManagerMapper.delete(wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SysUser saveAreaManager(AreaManagerCreateReqVO managerCreateReqVO) throws Exception {
        SysUser sysUser = SysUserConvert.INSTANCE.convertVO(managerCreateReqVO);
        if (StringUtils.isNotBlank(managerCreateReqVO.getUsername())) {
            sysUser.setUsername(managerCreateReqVO.getUsername());
        }
        sysUser.setApproveStatus(ApproveStatus.TO_BE_APPROVED.getCode());
        // 保存用户账号数据
        boolean result = saveUserPublic(sysUser, RoleEnum.region_admin);

        // 保存用户角色数据
        if (!result) {
            return null;
        }
        sysUser.setPassword(null);
        sysUser.setKey(null);
        return sysUser;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateAreaManager(AreaManagerUpdateReqVO updateVO) throws Exception {
        SysUser user = this.selectSysUserById(updateVO.getId());
        Assert.notNull(user, USER_NOT_EXISTS.getMessage());
        user.setAreaCode(updateVO.getAreaCode());
        user.setName(updateVO.getName());
        user.setSex(updateVO.getSex());
        user.setMobile(updateVO.getMobile());
        user.setEmail(updateVO.getEmail());
        user.setEnabled(updateVO.getEnabled());
        user.setApproveStatus(ApproveStatus.APPROVED.getCode());
        updateUserPublic(user);
        cacheUser(user.getId());
        return true;
    }

    @Override
    public void deleteAreaManager(Set<Long> ids) {
        delUser(ids);
    }

    @Override
    public boolean saveUserPublic(SysUser sysUser, RoleEnum roleEnum) throws Exception {
        String mobile = sysUser.getMobile();
        if (StrUtil.isEmpty(sysUser.getUsername()) && StrUtil.isNotEmpty(mobile)) {
            sysUser.setUsername(mobile);
            sysUser.setPassword(EncryptorUtil.encrypt(mobile.substring(mobile.length() - 6)));
        }

        sysUser.setId(null);
        sysUser.setKey(key);
        sysUser.setEnabled(Boolean.TRUE);
        sysUser.setRoleCode(roleEnum.getValue());
        if (Objects.isNull(sysUser.getApproveStatus())) {
            sysUser.setApproveStatus(ApproveStatus.APPROVED.getCode());
        }

        if (ObjectUtils.isEmpty(sysUser.getSex())) {
            sysUser.setSex(Sex.UNKNOWN.getCode());
        }

        String username = sysUser.getUsername();
        String lockKey = UserConstant.LOCK_KEY_USERNAME + new Date();
        if (lock == null) {
            throw new LockException("IDistLock is null");
        }

        if (StrUtil.isEmpty(lockKey)) {
            throw new LockException("lockKey is null");
        }

        int result;
        Object locker = null;
        try {
            //加锁
            locker = lock.tryLock(lockKey, CommonConstant.WAIT_TIME, CommonConstant.LEASE_TIME, TimeUnit.SECONDS);
            if (locker != null) {
                //判断记录是否已存在
                SysUser existUser = baseMapper.getUserByUsernameRoleAreaSchool(username, roleEnum.getValue(), sysUser.getAreaCode(), sysUser.getSchoolId(), key);
                if (Objects.nonNull(existUser)) {
                    throw new IdempotencyException(username + "已存在");
                } else {
                    result = baseMapper.insertBatch(Collections.singletonList(sysUser), key);
                }
            } else {
                throw new LockException("锁等待超时");
            }
        } finally {
            lock.unlock(locker);
        }

        boolean flag = result > 0;
        if (flag) {
            //将新添加的用户名及手机号放入过滤器中
            String transactionId = UUID.randomUUID().toString();
            TransactionMsgDefinationDTO msgDefinationDTO = new TransactionMsgDefinationDTO();
            msgDefinationDTO.setDestClass(this.getClass());
            msgDefinationDTO.setMethod("doSaveUserPublic");
            msgDefinationDTO.setArg(sysUser);
            Set<String> params = new HashSet<>();
            params.add(sysUser.getUsername());
            params.add(mobile);
            TransactionSendResult sendResult = rocketMQTemplate.sendMessageInTransaction(Constant.ADD_WARNING_GROUP, RocketMQConstant.ADD_USER_FILTER_TOPIC,
                MessageBuilder.withPayload(params).setHeader(RocketMQHeaders.TRANSACTION_ID, transactionId).build(), msgDefinationDTO);
            if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                //mq 异常
                throw exception("系统繁忙,请稍后重试");
            }
            if (LocalTransactionState.ROLLBACK_MESSAGE.equals(sendResult.getLocalTransactionState())) {
                //数据库异常
                throw exception("系统繁忙,请稍后重试");
            }
        } else {
            baseMapper.fullData(sysUser.getId());
        }
        return flag;
    }

    @Override
    @Transactional
    public void doSaveUserPublic(SysUser sysUser, String transactionId) {
        baseMapper.fullData(sysUser.getId());
        //记录mq事务日志
        RocketmqTransactionLog mqLog = new RocketmqTransactionLog();
        //这个log字段自定义消息，系统不会使用
        mqLog.setLog("sysUser json ： " + JsonUtils.writeValueAsString(sysUser));
        mqLog.setTransactionId(transactionId);
        rocketmqTransactionLogMapper.insert(mqLog);
    }

    @Override
    public boolean updateUserPublic(SysUser sysUser) throws Exception {
        userExists(sysUser.getId());
        sysUser.setKey(key);

        SysUserDTO dto = findByUserId(sysUser.getId(), false);
        String presetRoleCode = dto.getPresetRoleCode();
        if (!(RoleEnum.admin.getValue().equals(presetRoleCode) || RoleEnum.first_level_admin.getValue().equals(presetRoleCode) || RoleEnum.second_level_admin.getValue()
            .equals(presetRoleCode) || RoleEnum.test_admin.getValue().equals(presetRoleCode))) {
            if (sysUser.getAreaCode() == null && dto.getAreaCode() != null) {
                sysUser.setAreaCode(dto.getAreaCode());
            }

            if (sysUser.getSchoolId() == null && dto.getSchoolId() != null) {
                sysUser.setSchoolId(dto.getSchoolId());
            }
        }

        boolean result;
        String lockKey = UserConstant.LOCK_KEY_USERNAME + sysUser.getId();
        Object locker = null;
        try {
            //加锁
            locker = lock.tryLock(lockKey, CommonConstant.WAIT_TIME, CommonConstant.LEASE_TIME, TimeUnit.SECONDS);
            if (locker != null) {
                String mobile = sysUser.getMobile();
                if (StrUtil.isNotEmpty(mobile) && !mobile.equals(dto.getMobile())) {
                    if (dto.getUsername().equals(dto.getMobile())) {
                        sysUser.setUsername(mobile);
                    }
                }

                //判断记录是否已存在
                SysUser existUser = baseMapper.getUserByUsernameRoleAreaSchool(sysUser.getUsername(), presetRoleCode, sysUser.getAreaCode(), sysUser.getSchoolId(), key);
                if (Objects.nonNull(existUser) && !existUser.getId().equals(sysUser.getId())) {
                    throw new IdempotencyException(sysUser.getUsername() + "已存在");
                }

                sysUser.setUpdateBy(SysUserUtil.getHeaderUserId());
                sysUser.setUpdateTime(new Date());
                //更新密码
                if (sysUser.getMobile() != null && !RoleEnum.student.getValue().equals(dto.getPresetRoleCode())) {
                    if (dto.getFirstLogin() && !sysUser.getMobile().equals(dto.getMobile())) {
                        sysUser.setPassword(EncryptorUtil.encrypt(sysUser.getMobile().substring(sysUser.getMobile().length() - 6)));
                    }
                }

                if (StrUtil.isNotEmpty(mobile) && !mobile.equals(dto.getMobile())) {
                    //将更新的用户名及手机号放入过滤器中
                    String transactionId = UUID.randomUUID().toString();
                    TransactionMsgDefinationDTO msgDefinationDTO = new TransactionMsgDefinationDTO();
                    msgDefinationDTO.setDestClass(this.getClass());
                    msgDefinationDTO.setMethod("doUpdateUserPublic");
                    msgDefinationDTO.setArg(sysUser);
                    Set<String> params = new HashSet<>();
                    params.add(sysUser.getUsername());
                    params.add(mobile);
                    TransactionSendResult sendResult = rocketMQTemplate.sendMessageInTransaction(Constant.ADD_WARNING_GROUP, RocketMQConstant.ADD_USER_FILTER_TOPIC,
                        MessageBuilder.withPayload(params).setHeader(RocketMQHeaders.TRANSACTION_ID, transactionId).build(), msgDefinationDTO);
                    if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                        //mq 异常
                        throw exception("系统繁忙,请稍后重试");
                    }
                    if (LocalTransactionState.ROLLBACK_MESSAGE.equals(sendResult.getLocalTransactionState())) {
                        //数据库异常
                        throw exception("系统繁忙,请稍后重试");
                    }
                    result = true;
                } else {
                    result = baseMapper.updateBatch(Collections.singletonList(sysUser), key) > 0;
                }
            } else {
                throw new LockException("锁等待超时");
            }
        } finally {
            lock.unlock(locker);
        }

        return result;
    }

    @Transactional
    public void doUpdateUserPublic(SysUser sysUser, String transactionId) throws Exception {
        baseMapper.updateBatch(Collections.singletonList(sysUser), key);
        //记录mq事务日志
        RocketmqTransactionLog mqLog = new RocketmqTransactionLog();
        //这个log字段自定义消息，系统不会使用
        mqLog.setLog("sysUser json ： " + JsonUtils.writeValueAsString(sysUser));
        mqLog.setTransactionId(transactionId);
        rocketmqTransactionLogMapper.insert(mqLog);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void setRoleToUser(Long userId, List<String> roleCodes) {
        SysUserDTO user = findByUserId(userId, false);
        Assert.notNull(user, USER_NOT_EXISTS.getMessage());

        if (CollUtil.isNotEmpty(roleCodes)) {
            //内置角色
            String presetRoleCode = roleCodes.stream().filter(roleCode -> RoleEnum.presetRole(roleCode).equals(Boolean.TRUE)).findFirst().get();
            //自定义角色
            List<String> customRoleCodes = roleCodes.stream().filter(roleCode -> RoleEnum.presetRole(roleCode).equals(Boolean.FALSE)).collect(Collectors.toList());

            //更新内置角色
            if (StringUtils.isNotBlank(presetRoleCode)) {
                SysUser sysUser = new SysUser();
                sysUser.setId(user.getId());
                sysUser.setRoleCode(presetRoleCode);
                baseMapper.updateById(sysUser);
                cacheUser(userId);
            }

            if (CollectionUtils.isNotEmpty(customRoleCodes)) {
                userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
                saveUserRole(userId, customRoleCodes);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void setAuthorityToUser(Long id, Set<SysUserAuthorityReqVO> userAuthority) {
        userExists(id);

        Map<String, Object> columnMap = new HashMap<>(2);
        columnMap.put("user_id", id);
        userAuthorityMapper.deleteByMap(columnMap);

        if (!CollectionUtils.isEmpty(userAuthority)) {
            List<SysUserAuthority> roleAuthorities = new ArrayList<>();
            for (SysUserAuthorityReqVO reqVO : userAuthority) {
                SysAuthority authority = authorityService.getOne(new LambdaQueryWrapper<SysAuthority>().eq(SysAuthority::getCode, reqVO.getAuthorityCode()));
                if (Objects.isNull(authority)) {
                    continue;
                }
                roleAuthorities.add(new SysUserAuthority(id, authority.getId(), reqVO.getIsOwned()));
            }
            userAuthorityService.saveBatch(roleAuthorities);
        }

        cacheUser(id);
    }

    @Override
    public void updateEnabled(Long id, Boolean enabled) {
        SysUserDTO existUser = findByUserId(id, false);
        Assert.notNull(existUser, USER_NOT_EXISTS.getMessage());

        // 禁用，则移除该用户的登录状态
        Map<String, Object> params = new HashMap<>();
        params.put("existUser", existUser);
        params.put("enabled", enabled);
        if (!enabled) {
            String transactionId = UUID.randomUUID().toString();
            TransactionMsgDefinationDTO msgDefinationDTO = new TransactionMsgDefinationDTO();
            msgDefinationDTO.setDestClass(this.getClass());
            msgDefinationDTO.setMethod("doUpdateEnabled");
            msgDefinationDTO.setArg(params);
            Set<SysUserDTO> userDTOS = new HashSet<>();
            userDTOS.add(existUser);
            //三个业务参数  dto-->消费端用参数    userDynamic-->当前业务参数  transactionId-->事务消息唯一编码
            TransactionSendResult sendResult = rocketMQTemplate.sendMessageInTransaction(Constant.ADD_WARNING_GROUP, RocketMQConstant.REMOVE_LOGIN_TOPIC,
                MessageBuilder.withPayload(userDTOS).setHeader(RocketMQHeaders.TRANSACTION_ID, transactionId).build(), msgDefinationDTO);
            if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                //mq 异常
                throw exception("系统繁忙,请稍后重试");
            }
            if (LocalTransactionState.ROLLBACK_MESSAGE.equals(sendResult.getLocalTransactionState())) {
                //数据库异常
                throw exception("系统繁忙,请稍后重试");
            }
        } else {
            doUpdateEnabled(params, null);
        }
    }

    @Override
    @Transactional
    public void doUpdateEnabled(Map<String, Object> params, String transactionId) {
        SysUserDTO existUser = (SysUserDTO)params.get("existUser");
        Long id = existUser.getId();
        Boolean enabled = (Boolean)params.get("enabled");
        String roleCode = existUser.getPresetRoleCode();

        SysUser sysUser = new SysUser();
        sysUser.setId(id);
        sysUser.setEnabled(enabled);
        sysUser.setUpdateBy(SysUserUtil.getHeaderUserId());
        sysUser.setUpdateTime(new Date());
        baseMapper.updateBatch(Collections.singletonList(sysUser), key);

        // 同步更新学生基础信息表启用状态
        if (RoleEnum.student.getValue().equals(roleCode)) {
            LambdaQueryWrapper<StudentBaseInfo> existedQuery = new LambdaQueryWrapper<>();
            existedQuery.eq(StudentBaseInfo::getUserId, id);
            StudentBaseInfo studentBaseInfo = studentMapper.selectOne(existedQuery);
            if (Objects.nonNull(studentBaseInfo)) {
                UpdateWrapper<StudentBaseInfo> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("id", studentBaseInfo.getId()).set("enabled", enabled).set("update_by", SysUserUtil.getHeaderUserId()).set("update_time", new Date());
                studentMapper.update(null, updateWrapper);
            }
        }

        // 同步更新家长表启用状态
        if (RoleEnum.parents.getValue().equals(roleCode)) {
            LambdaQueryWrapper<Parent> existedQuery = new LambdaQueryWrapper<>();
            existedQuery.eq(Parent::getUserId, id);
            Parent parent = parentMapper.selectOne(existedQuery);
            if (Objects.nonNull(parent)) {
                UpdateWrapper<Parent> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("id", parent.getId()).set("enabled", enabled).set("update_by", SysUserUtil.getHeaderUserId()).set("update_time", new Date());
                parentMapper.update(null, updateWrapper);
            }
        }

        cacheUser(sysUser.getId());

        if (transactionId != null) {
            //记录mq事务日志
            RocketmqTransactionLog mqLog = new RocketmqTransactionLog();
            //这个log字段自定义消息，系统不会使用
            mqLog.setLog("update user enable id： " + id + ", name: " + existUser.getName());
            mqLog.setTransactionId(transactionId);
            rocketmqTransactionLogMapper.insert(mqLog);
        }
    }

    @Override
    public void resetPassword(Long id) {
        String defaultPwd = CommonConstant.DEFAULT_USER_PASSWORD;
        SysUserDTO userDTO = findByUserId(id, false);
        Assert.notNull(userDTO, USER_NOT_EXISTS.getMessage());

        String roleCode = userDTO.getPresetRoleCode();
        String username = userDTO.getUsername();
        if (RoleEnum.student.getValue().equals(roleCode)) {
            if (TextValidator.isIdCard(username)) {
                defaultPwd = username.substring(username.length() - 6);
            }
        } else {
            if (TextValidator.isMobileExact(username)) {
                defaultPwd = username.substring(username.length() - 6);
            }
        }

        SysUser sysUser = new SysUser();
        sysUser.setKey(key);
        sysUser.setId(id);
        // 密码存储方式：RSA公钥加密后存入数据库，数据库采用AES加解密
        sysUser.setPassword(EncryptorUtil.encrypt(defaultPwd));
        sysUser.setUpdateBy(SysUserUtil.getHeaderUserId());
        sysUser.setUpdateTime(new Date());
        baseMapper.updateBatch(Collections.singletonList(sysUser), key);

        cacheUser(sysUser.getId());
    }

    /**
     * 忘记密码
     *
     * @param forgetPassword 请求参数
     */
    @Override
    public void forgetPassword(ForgetPasswordVO forgetPassword) {
        //判断密码和确认密码是否相等
        String newPassword = forgetPassword.getNewPassword();
        String confirmPassword = forgetPassword.getConfirmPassword();

        String decryptNewPassword;
        String decryptConfirmPassword;
        if (forgetPassword.getFlag() != null && forgetPassword.getFlag()) {
            decryptNewPassword = newPassword;
            decryptConfirmPassword = confirmPassword;
        } else {
            try {
                decryptNewPassword = EncryptorUtil.decrypt(newPassword);
            } catch (Exception e) {
                e.printStackTrace();
                throw exception("新密码错误");
            }
            try {
                decryptConfirmPassword = EncryptorUtil.decrypt(confirmPassword);
            } catch (Exception e) {
                e.printStackTrace();
                throw exception("确认密码错误");
            }
        }

        if (!decryptNewPassword.equals(decryptConfirmPassword)) {
            throw exception("新密码和确认密码不一致");
        }

        //查询短信发送记录
        SmsRecord smsRecord = smsRecordMapper.selectFirst(forgetPassword.getMobile(), forgetPassword.getVerifyCode());
        if (null == smsRecord || null == smsRecord.getUserId()) {
            throw exception("验证码错误");
        }
        if (smsRecord.getUsed() == 1) {
            throw exception("验证码已使用");
        }
        if (new Date().after(smsRecord.getExpireTime())) {
            throw exception("验证码已失效");
        }

        Long userId = forgetPassword.getChoseUserId();
        Assert.isTrue(userId != null && userId > 0, "未指定用户");

        SysUser sysUser = new SysUser();
        sysUser.setKey(key);
        sysUser.setId(userId);
        // 密码存储方式：RSA公钥加密后存入数据库，数据库采用AES加解密
        sysUser.setPassword(newPassword);
        sysUser.setUpdateBy(userId);
        sysUser.setUpdateTime(new Date());
        baseMapper.updateBatch(Collections.singletonList(sysUser), key);

        cacheUser(sysUser.getId());

        smsRecord.setUsed(1);
        smsRecordMapper.updateById(smsRecord);
    }

    @Override
    public List<SysUserDTO> findLoginUserByMobile(String mobile, String captcha, boolean h5) {
        List<SysUserDTO> result = findLoginUserByMobile(mobile, captcha);
        if (!h5) {
            return result;
        }

        result.removeIf(user -> !(user.getPresetRoleCode().equals(RoleEnum.student.getValue()) || user.getPresetRoleCode().equals(RoleEnum.parents.getValue())));

        return result;
    }

    private List<SysUserDTO> findLoginUserByMobile(String mobile, String captcha) {
        //查询短信发送记录
        SmsRecord smsRecord = smsRecordMapper.selectFirst(mobile, captcha);
        if (Objects.isNull(smsRecord)) {
            throw exception("验证码错误");
        }
        if (smsRecord.getUsed() == 1) {
            throw exception("验证码已使用");
        }
        if (new Date().after(smsRecord.getExpireTime())) {
            throw exception("验证码已失效");
        }
        List<SysUser> models = baseMapper.findUserByUsernameOrMobileOrShortId(mobile, key, "mobile");
        List<SysUserDTO> result = new ArrayList<>(models.size());
        models.forEach(model -> result.add(findSysUser(model)));
        return result;
    }

    @Override
    public Boolean updatesSmsRecordUsedStatus(String mobile, String captcha) {
        //查询短信发送记录
        SmsRecord smsRecord = smsRecordMapper.selectFirst(mobile, captcha);
        if (Objects.isNull(smsRecord)) {
            return Boolean.FALSE;
        }

        smsRecord.setUsed(1);
        smsRecordMapper.updateById(smsRecord);
        return Boolean.TRUE;
    }

    @Override
    public void updatePassword(UpdatePasswordReqVO updatePasswordReqVO) {
        Long userId = UserUtil.getUserId();
        SysUser user = selectSysUserById(userId);
        Assert.notNull(user, USER_NOT_EXISTS.getMessage());

        String oldPassWord = EncryptorUtil.decrypt(updatePasswordReqVO.getOldPassword());
        String dbPassword = EncryptorUtil.decrypt(user.getPassword());
        if (!oldPassWord.equals(dbPassword)) {
            throw exception(USER_OLD_PASSWORD_ERROR);
        }

        SysUser sysUser = new SysUser();
        sysUser.setKey(key);
        sysUser.setId(userId);
        sysUser.setPassword(updatePasswordReqVO.getNewPassword());
        if (Boolean.TRUE.equals(user.getFirstLogin())) {
            sysUser.setFirstLogin(false);
        }
        sysUser.setUpdateBy(userId);
        sysUser.setUpdateTime(new Date());
        baseMapper.updateBatch(Collections.singletonList(sysUser), key);

        cacheUser(sysUser.getId());
    }

    @Override
    public PageResult<SysUserDTO> findUsers(Map<String, Object> params) {
        Page<SysUser> page = new Page<>(MapUtil.getInt(params, Constant.PAGE_NUM, 1), MapUtil.getInt(params, Constant.PAGE_SIZE, 10));
        UserUtil.setSearchParams(params);

        boolean schoolManager = false;
        boolean areaManager = false;
        if (params.containsKey("roleCode")) {
            String roleCode = params.get("roleCode").toString();
            params.put("roleCodes", roleCode.split(","));
            if (roleCode.contains(RoleEnum.school_admin.getValue())) {
                schoolManager = true;
            } else if (roleCode.contains(RoleEnum.region_admin.getValue())) {
                areaManager = true;
            }
        }

        List<SysUser> userList;
        if (schoolManager) {
            userList = baseMapper.findSchoolManagerList(page, params, key);
        } else if (areaManager) {
            userList = baseMapper.findAreaManagerList(page, params, key);
        } else {
            userList = baseMapper.findList(page, params, key);
        }

        List<SysUserDTO> sysUsers = new ArrayList<>();
        long total = page.getTotal();
        if (total > 0 && CollectionUtils.isNotEmpty(userList)) {
            for (SysUser userData : userList) {
                SysUserDTO user = SysUserConvert.INSTANCE.convert(userData);
                Long userId = user.getId();
                user.setPassword(null);

                // 设置自定义角色
                List<SysRole> customRoles = userRoleService.findRolesByUserId(userId);
                if (CollectionUtils.isNotEmpty(customRoles)) {
                    Set<String> customRoleCodes = customRoles.stream().map(SysRole::getCode).collect(Collectors.toSet());
                    user.setCustomRoleCodes(customRoleCodes);
                }

                // 账号名(手机号、身份证号)脱敏
                SysUserUtil.desensitizedUserInfo(user);
                sysUsers.add(user);
            }
        }

        return PageResult.<SysUserDTO>builder().data(sysUsers).count(total).build();
    }

    @Override
    public PageResult<SysUserDTO> findTestUsers(Map<String, Object> params) {
        Page<SysUser> page = new Page<>(MapUtil.getInt(params, Constant.PAGE_NUM, 1), MapUtil.getInt(params, Constant.PAGE_SIZE, 10));
        UserUtil.setSearchParams(params);

        List<SysUser> userList = baseMapper.findTestManagerList(page, params, key);
        List<SysUserDTO> sysUsers = new ArrayList<>();
        long total = page.getTotal();
        if (total > 0 && CollectionUtils.isNotEmpty(userList)) {
            for (SysUser userData : userList) {
                SysUserDTO user = SysUserConvert.INSTANCE.convert(userData);
                Long userId = user.getId();
                user.setPassword(null);

                // 设置自定义角色
                List<SysRole> customRoles = userRoleService.findRolesByUserId(userId);
                if (CollectionUtils.isNotEmpty(customRoles)) {
                    Set<String> customRoleCodes = customRoles.stream().map(SysRole::getCode).collect(Collectors.toSet());
                    user.setCustomRoleCodes(customRoleCodes);
                }

                List<SchoolDTO> schools = testManagerMapper.findSchoolList(userId, null);
                user.setSchools(schools);

                // 账号名(手机号、身份证号)脱敏
                SysUserUtil.desensitizedUserInfo(user);
                sysUsers.add(user);
            }
        }

        return PageResult.<SysUserDTO>builder().data(sysUsers).count(total).build();
    }

    /**
     * 设置内置角色/自定义角色
     *
     * @param userDTO   用户对象
     * @param roleCodes 角色编码列表
     */
    private void setRoles(SysUserDTO userDTO, Set<String> roleCodes) {
        Set<String> customRoleCodes = new HashSet<>();
        for (String roleCode : roleCodes) {
            if (Boolean.TRUE.equals(RoleEnum.presetRole(roleCode))) {
                // 设置内置角色
                userDTO.setPresetRoleCode(roleCode);
            } else {
                customRoleCodes.add(roleCode);
            }
        }
        // 设置自定义角色
        userDTO.setCustomRoleCodes(customRoleCodes);
    }

    /**
     * SysUserDTO
     *
     * @param sysUser 用户对象
     * @return 用户DTO
     */
    private SysUserDTO findSysUser(SysUser sysUser) {
        if (sysUser != null) {
            SysUserDTO userDTO = SysUserConvert.INSTANCE.convert(sysUser);
            Long userId = userDTO.getId();
            Set<String> roleCodes = new HashSet<>();
            roleCodes.add(userDTO.getPresetRoleCode());

            // 设置自定义角色
            /*List<SysRole> customRoles = userRoleService.findRolesByUserId(sysUser.getId());
            if (CollectionUtils.isNotEmpty(customRoles)) {
                Set<String> customRoleCodes = customRoles.stream().map(SysRole::getCode).collect(Collectors.toSet());
                userDTO.setCustomRoleCodes(customRoleCodes);
                roleCodes.addAll(customRoleCodes);
            }*/

            Integer areaCode = userDTO.getAreaCode();
            if (Objects.nonNull(areaCode)) {
                SysAreaDTO area = RedisUtils.getAreaByCache(areaCode);
                if (Objects.nonNull(area)) {
                    userDTO.setAreaName(area.getName());
                }
            }

            // 查询所有学校信息
            Map<Long, SchoolDTO> schoolMap = DataCacheUtil.getSchoolMap();

            // 设置学校信息(校级管理员、学生、家长、校心理老师、校教职工/校领导/班主任)
            if (Objects.nonNull(userDTO.getSchoolId()) && schoolMap.containsKey(userDTO.getSchoolId())) {
                userDTO.setSchoolName(schoolMap.get(userDTO.getSchoolId()).getName());
            }

            if (RoleEnum.parents.getValue().equals(userDTO.getPresetRoleCode())) {
                Map<Long, Map<String, Boolean>> confirmAndActive = parentMapper.getConfirmAndActive(Collections.singletonList(userId));
                // 设置家长激活和确认状态
                if (Objects.nonNull(confirmAndActive) && confirmAndActive.size() > 0 && confirmAndActive.containsKey(userId)) {
                    userDTO.setIsActive(confirmAndActive.get(userId).get("active"));
                    userDTO.setConfirmed(confirmAndActive.get(userId).get("confirmed"));
                }
            }

            if (RoleEnum.region_psycho_teacher.getValue().equals(userDTO.getPresetRoleCode())) {
                AreaTeacher areaTeacher = areaTeacherMapper.selectOne(Wrappers.lambdaQuery(AreaTeacher.class).eq(AreaTeacher::getUserId, userId));
                if (Objects.nonNull(areaTeacher)) {
                    userDTO.setIsAcceptTask(areaTeacher.getIsAcceptTask());
                }
            }

            if (RoleEnum.school_psycho_teacher.getValue().equals(userDTO.getPresetRoleCode())) {
                SchoolTeacher schoolTeacher = schoolTeacherMapper.selectOne(Wrappers.lambdaQuery(SchoolTeacher.class).eq(SchoolTeacher::getUserId, userId));
                if (Objects.nonNull(schoolTeacher)) {
                    userDTO.setIsAcceptTask(schoolTeacher.getIsAcceptTask());
                }
            }

            if (RoleEnum.test_admin.getValue().equals(userDTO.getPresetRoleCode())) {
                List<SchoolDTO> schools = testManagerMapper.findSchoolList(userId, ApproveStatus.APPROVED.getCode());
                userDTO.setSchools(schools);
            }

            // 角色权限
            List<SysAuthority> roleAuthorities = new ArrayList<>();
            roleCodes.forEach(roleCode -> roleAuthorities.addAll(DataCacheUtil.getAuthoritiesByRoleCode(roleCode)));

            // 用户权限
            //List<SysAuthority> userAuthorities = userAuthorityService.findAuthoritiesByUserId(sysUser.getId());

            // 设置用户最终拥有的权限
            setUserAuthorities(userDTO, roleAuthorities, null);

            // 性别重写，如果数据的sex为null，赋予默认值0
            if (userDTO.getSex() == null) {
                userDTO.setSex(Sex.UNKNOWN.getCode());
            }
            return userDTO;
        }
        return null;
    }

    @Override
    public List<BatchOperationTipDTO> delUser(Set<Long> ids) {
        List<BatchOperationTipDTO> resultMap = new ArrayList<>();
        Set<SysUser> users = new HashSet<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            List<Long> delUser = new ArrayList<>();
            for (Long id : ids) {
                SysUser sysUser = baseMapper.selectOneById(key, id);
                if (ObjectUtils.isEmpty(sysUser)) {
                    resultMap.add(new BatchOperationTipDTO(id, "无效id"));
                    continue;
                }
                delUser.add(id);
                users.add(sysUser);
            }
            if (CollectionUtils.isNotEmpty(delUser)) {
                // 删除评测记录、自测、普测、评测预警
                String transactionId = UUID.randomUUID().toString();
                TransactionMsgDefinationDTO msgDefinationDTO = new TransactionMsgDefinationDTO();
                msgDefinationDTO.setDestClass(this.getClass());
                msgDefinationDTO.setMethod("removeLogin");
                msgDefinationDTO.setArg(users);
                //三个业务参数  dto-->消费端用参数    userDynamic-->当前业务参数  transactionId-->事务消息唯一编码
                TransactionSendResult sendResult = rocketMQTemplate.sendMessageInTransaction(Constant.ADD_WARNING_GROUP, RocketMQConstant.DELETE_USERS_TOPIC,
                    MessageBuilder.withPayload(delUser).setHeader(RocketMQHeaders.TRANSACTION_ID, transactionId).build(), msgDefinationDTO);
                if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                    //mq 异常
                    throw exception("系统繁忙,请稍后重试");
                }
                if (LocalTransactionState.ROLLBACK_MESSAGE.equals(sendResult.getLocalTransactionState())) {
                    //数据库异常
                    throw exception("系统繁忙,请稍后重试");
                }
            }
        }
        return resultMap;
    }

    @Transactional
    public void removeLogin(Set<SysUser> users, String transactionId) {
        // 移除用户登录状态
        String transactionId_1 = UUID.randomUUID().toString();
        TransactionMsgDefinationDTO msgDefinationDTO = new TransactionMsgDefinationDTO();
        msgDefinationDTO.setDestClass(this.getClass());
        msgDefinationDTO.setMethod("doDelUser");
        msgDefinationDTO.setArg(users);
        Set<SysUserDTO> userDTOS = new HashSet<>();
        users.stream().forEach(e -> {
            SysUserDTO dto = new SysUserDTO();
            dto.setId(e.getId());
            dto.setUsername(e.getUsername());
            dto.setMobile(e.getMobile());
            dto.setPresetRoleCode(e.getRoleCode());
            dto.setShortId(e.getShortId());
            userDTOS.add(dto);
        });
        //三个业务参数  dto-->消费端用参数    userDynamic-->当前业务参数  transactionId-->事务消息唯一编码
        TransactionSendResult sendResult = rocketMQTemplate.sendMessageInTransaction(Constant.ADD_WARNING_GROUP, RocketMQConstant.REMOVE_LOGIN_TOPIC,
            MessageBuilder.withPayload(userDTOS).setHeader(RocketMQHeaders.TRANSACTION_ID, transactionId_1).build(), msgDefinationDTO);
        if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
            //mq 异常
            throw exception("系统繁忙,请稍后重试");
        }
        if (LocalTransactionState.ROLLBACK_MESSAGE.equals(sendResult.getLocalTransactionState())) {
            //数据库异常
            throw exception("系统繁忙,请稍后重试");
        }
        //记录mq事务日志
        RocketmqTransactionLog mqLog = new RocketmqTransactionLog();
        //这个log字段自定义消息，系统不会使用
        String log = "delUser json ： " + JsonUtils.writeValueAsString(users);
        if (log.length() > RocketMQConstant.LOG_LENGTH) {
            log = log.substring(0, RocketMQConstant.LOG_LENGTH - 3) + "...";
        }
        mqLog.setLog(log);
        mqLog.setTransactionId(transactionId);
        rocketmqTransactionLogMapper.insert(mqLog);
    }

    @Override
    @Transactional
    public void doDelUser(Set<SysUser> users, String transactionId) {
        List<Long> delUser = new ArrayList<>();
        for (SysUser sysUser : users) {
            Long id = sysUser.getId();
            Map<String, Object> columnMap = new HashMap<>(2);
            columnMap.put("user_id", id);

            // 逻辑删除
            baseMapper.deleteById(id);
            userRoleMapper.deleteByMap(columnMap);
            // 删除用户权限
            userAuthorityMapper.deleteByMap(columnMap);
            // 删除用户管理的学校(校级管理员)
            schoolManagerMapper.deleteByMap(columnMap);
            // 删除用户管理的学校(测试管理员)
            testManagerMapper.deleteByMap(columnMap);
            // 删除用户管理的学校的密码(测试管理员)
            testManagerPasswordMapper.deleteByMap(columnMap);
            // 删除用户动态
            userDynamicMapper.deleteByMap(columnMap);
            // 删除站内信
            messageMapper.deleteByMap(columnMap);

            // 删除缓存数据
            RedisUtils.deleteCache(RedisConstant.USER_SUFFIX + sysUser.getId());

            delUser.add(id);
        }

        // 删除用户预约咨询报告
        List<NewReservation> newReservations = reservationMapper.selectList(Wrappers.lambdaQuery(NewReservation.class).in(NewReservation::getUserId, delUser));
        if (CollUtil.isNotEmpty(newReservations)) {
            List<Long> reservationIds = newReservations.stream().map(NewReservation::getId).collect(Collectors.toList());
            consultationReportMapper.delete(Wrappers.lambdaQuery(ConsultationReport.class).in(ConsultationReport::getReservationId, reservationIds));
        }
        // 删除用户预约信息
        reservationMapper.delete(Wrappers.lambdaQuery(NewReservation.class).in(NewReservation::getUserId, delUser));

        //记录mq事务日志
        RocketmqTransactionLog mqLog = new RocketmqTransactionLog();
        //这个log字段自定义消息，系统不会使用
        String log = "delUser json ： " + JsonUtils.writeValueAsString(users);
        if (log.length() > RocketMQConstant.LOG_LENGTH) {
            log = log.substring(0, RocketMQConstant.LOG_LENGTH - 3) + "...";
        }
        mqLog.setLog(log);
        mqLog.setTransactionId(transactionId);
        rocketmqTransactionLogMapper.insert(mqLog);
    }

    @Override
    public SysUserDTO findByUserId(Long id, Boolean cacheData) {
        if (id == null || id == 0) {
            return null;
        }

        SysUserDTO userDTO;
        userDTO = RedisUtils.getUserByCache(id);
        if (Objects.isNull(userDTO)) {
            userDTO = findSysUser(baseMapper.selectOneById(key, id));
            if (Boolean.TRUE.equals(cacheData)) {
                if (Objects.isNull(userDTO)) {
                    RedisUtils.cacheEmpty(RedisConstant.USER_SUFFIX + id);
                } else {
                    RedisUtils.cacheUser(userDTO);
                }
            }
        }
        return userDTO;
    }

    @Override
    public String findRoleCodeById(Long id) {
        if (id == null || id == 0) {
            return StringUtils.EMPTY;
        }

        SysUserDTO userDTO = RedisUtils.getUserByCache(id);
        if (Objects.nonNull(userDTO)) {
            return userDTO.getPresetRoleCode();
        } else {
            return baseMapper.selectRoleCode(id);
        }
    }

    @Override
    public List<SysAuthority> findAuthoritiesByUserId(Long userId) {
        return userAuthorityService.findAuthoritiesByUserId(userId);
    }

    /**
     * 用户id查询
     *
     * @param id 用户ID
     * @return 用户对象
     */
    @Override
    public SysUser selectSysUserById(Long id) {
        Assert.notNull(id);
        SysUser sysUser = baseMapper.selectOneById(key, id);
        Assert.notNull(sysUser, "无效的用户ID");
        return sysUser;
    }

    /**
     * 根据用户ID,判断用户是否存在
     *
     * @param id 用户ID
     */
    private void userExists(Long id) {
        Assert.notNull(id);
        Integer count = baseMapper.selectCount(new QueryWrapper<SysUser>().eq("id", id).eq("is_deleted", 0));
        Assert.isTrue(count != null && count > 0, USER_NOT_EXISTS.getMessage());
    }

    @Override
    public void cacheUser(Long userId) {
        if (Objects.isNull(userId) || userId == 0L) {
            return;
        }

        SysUser sysUser = baseMapper.selectOneById(key, userId);
        if (Objects.isNull(sysUser)) {
            return;
        }

        if (Boolean.TRUE.equals(RedisUtils.existsKey(RedisConstant.USER_SUFFIX + userId))) {
            SysUserDTO sysUserDTO = findSysUser(sysUser);
            RedisUtils.cacheUser(sysUserDTO);
        }
    }

    /**
     * 设置用户最终拥有的权限
     *
     * @param userDTO            用户DTO
     * @param userAllAuthorities 用户所有权限
     * @param userAuthorities    用户权限
     */
    private void setUserAuthorities(SysUserDTO userDTO, List<SysAuthority> userAllAuthorities, List<SysAuthority> userAuthorities) {
        if (CollectionUtils.isNotEmpty(userAuthorities)) {
            for (SysAuthority authority : userAuthorities) {
                if (Boolean.TRUE.equals(authority.getIsOwned())) {
                    if (!userAllAuthorities.contains(authority)) {
                        userAllAuthorities.add(authority);
                    }
                } else {
                    userAllAuthorities.remove(authority);
                }
            }
        }

        Set<SysAuthoritySimpleDTO> permissions = new HashSet<>(SysAuthorityConvert.INSTANCE.convertSimpleList(userAllAuthorities));
        userDTO.setPermissions(permissions);
        userDTO.setPermissionCodes(permissions.stream().map(SysAuthoritySimpleDTO::getCode).collect(Collectors.toSet()));
    }

    @Override
    public void sendMessage(MessageDTO message) {
        Collection<Long> userIds = message.getUserIds();
        if (CollUtil.isNotEmpty(userIds)) {
            List<Message> models = new ArrayList<>(userIds.size());
            userIds.forEach(userId -> {
                Message model = new Message();
                model.setUserId(userId);
                model.setType(message.getType());
                model.setContent(message.getContent());
                model.setParams(message.getParams());
                model.setHasRead(false);
                model.setCreateBy(message.getCreateBy());
                model.setUpdateBy(message.getCreateBy());
                models.add(model);
            });
            messageService.saveBatch(models);
        }
    }

    @Override
    public List<Long> getUserIdListByRoleCode(String roleCode) {
        return baseMapper.getUserIdListByRoleId(roleCode, key);
    }

    @Override
    public RegionStaffInfoDTO getRegionStaffInfo(Long userId) {
        Assert.notNull(userId, USER_NO_ID.getMessage());
        return baseMapper.getRegionStaffInfo(userId);
    }

    /**
     * 查询任务的用户
     *
     * @param query 查询实体
     * @return 分页结果
     */
    @Override
    public PageResult<TaskUserResVO> getTaskUsers(TaskUserReqVO query) {
        Long userId = SysUserUtil.getHeaderUserId();

        Page<TaskUserResVO> page = new Page<>(query.getPageNum() != null ? query.getPageNum() : 1,
            query.getPageSize() != null ? Math.min(query.getPageSize(), CommonConstant.MAX_PAGE_SIZE) : CommonConstant.DEFAULT_PAGE_SIZE);
        long total;

        //获取当前用户的区域
        SysUserDTO sysUserDto = findByUserId(userId, false);
        Integer areaCode = sysUserDto.getAreaCode();
        //当前登录人角色
        String roleCode = sysUserDto.getPresetRoleCode();
        Long schoolId = 0L;
        //获取当前用户的学校id
        if (RoleEnum.school_psycho_teacher.getValue().equals(roleCode)) {
            schoolId = UserUtil.getSchoolId();
        }

        List<TaskUserResVO> taskUsers = new ArrayList<>();
        List<Long> userIds;
        Integer serviceObject = query.getServiceObject();
        if (serviceObject.equals(ServiceObject.STUDENT.getValue())) {
            query.setSchoolId(schoolId);
            taskUsers = studentMapper.getTaskUsers(page, query, key);
        } else if (serviceObject.equals(ServiceObject.PARENT.getValue())) {
            query.setSchoolId(schoolId);
            //查询家长及子女的信息
            taskUsers = parentMapper.getTaskUsers(page, query, key);
            if (CollectionUtils.isNotEmpty(taskUsers)) {
                //获取学校年级数据
                Map<String, String> dictMap = UserUtil.getDictData(UserUtil.SCHOOL_GRADE);
                for (TaskUserResVO taskUser : taskUsers) {
                    List<TaskUserResVO> students = taskUser.getStudents();
                    if (CollUtil.isNotEmpty(students)) {
                        students.forEach(student -> {
                            if (dictMap.containsValue(student.getGradeName())) {
                                String gradeName = UserUtil.getKey(dictMap, student.getGradeName());
                                if (StringUtils.isNotBlank(gradeName)) {
                                    student.setGradeName(gradeName);
                                }
                            }
                        });
                    }
                }
            }
        } else if (serviceObject.equals(ServiceObject.SCHOOL_STAFF.getValue())) {
            query.setSchoolId(schoolId);

            taskUsers = schoolStaffMapper.getTaskUsers(page, query, key);
        } else if (serviceObject.equals(ServiceObject.AREA_STAFF.getValue())) {
            //区域职员，测评需包括区域心理教研员
            query.setAreaCode(areaCode);
            taskUsers = areaStaffMapper.getTaskUsers(page, query, key);
        }

        total = page.getTotal();

        userIds = taskUsers.stream().map(TaskUserResVO::getUserId).collect(Collectors.toList());

        //账号信息
        List<SysUser> users = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(userIds)) {
            users = findListByIds(userIds);
        }

        for (TaskUserResVO taskUser : taskUsers) {
            for (SysUser sysUser : users) {
                if (taskUser.getUserId().equals(sysUser.getId())) {
                    taskUser.setName(sysUser.getName());
                    taskUser.setSex(Sex.getSex(sysUser.getSex()).getCode());
                    taskUser.setMobile(sysUser.getMobile());
                }
            }
        }

        taskUsers.forEach(taskUser -> {
            if (StrUtil.isNotEmpty(taskUser.getMobile())) {
                // 手机号脱敏
                taskUser.setMobile(DesensitizedUtil.desensitized(taskUser.getMobile(), DesensitizedUtil.DesensitizedType.MOBILE_PHONE));
            }
        });

        return PageResult.<TaskUserResVO>builder().data(taskUsers).count(total).build();
    }

    @Override
    public int saveBatch(List<SysUser> sysUsers) {
        return baseMapper.insertBatch(sysUsers, key);
    }

    @Override
    public List<SysUser> findListByIds(List<Long> ids) {
        return baseMapper.findListByIds(ids, key);
    }

    @Override
    public Boolean taskInScope(Long taskUserId) {
        //获取当前登录人信息
        SysUserDTO sysUserDTO = findByUserId(SysUserUtil.getHeaderUserId(), false);

        String roleCode = sysUserDTO.getPresetRoleCode();

        //区域心理教研员
        if (RoleEnum.region_psycho_teacher.getValue().equals(roleCode)) {
            //获取区域code
            Integer areaCode = sysUserDTO.getAreaCode();
            return areaStaffMapper.areaTaskInScope(areaCode, taskUserId) != null;
        } else if (RoleEnum.school_psycho_teacher.getValue().equals(roleCode)) {
            //校心理老师
            Long schoolId = sysUserDTO.getSchoolId();
            return schoolStaffMapper.schoolTaskInScope(schoolId, taskUserId) != null;
        } else {
            return false;
        }
    }

    @Override
    public Long findUserCount() {
        return baseMapper.selectCount(new QueryWrapper<>()).longValue();
    }

    @Override
    public List<String> findUsernameAndMobile(Integer limit, Integer offset) {
        return baseMapper.findUsernameAndMobile(Math.max(limit, 1), Math.max(offset, 0), key);
    }

    @Override
    public List<SysUserDTO> findUserByUsernameOrMobileOrShortId(String query) {
        List<SysUser> models;
        if (TextValidator.isMobileExact(query)) {
            models = baseMapper.findUserByUsernameOrMobileOrShortId(query, key, "mobile");
        } else if (TextValidator.isIdCard(query) || CommonConstant.SUPER_ADMIN_USER_NAME.equals(query)) {
            models = baseMapper.findUserByUsernameOrMobileOrShortId(query, key, "userName");
        } else {
            models = baseMapper.findUserByUsernameOrMobileOrShortId(query, key, "shortId");
        }

        List<SysUserDTO> result = new ArrayList<>(models.size());
        models.forEach(model -> result.add(findSysUser(model)));
        return result;
    }

    @Override
    public SysUserDTO findUserByUsernameWithTestManager(String query) {
        return findSysUser(baseMapper.findUserByUsernameWithTestManager(query, key));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void h5UpdateUserMobile(SysUserUpdateMobileReqVO vo) throws Exception {
        SysUserDTO user = UserUtil.getCurrentUser();
        Assert.notNull(user, "用户信息未找到");
        Long userId = user.getId();

        String mobile = vo.getMobile();
        String newMobile = vo.getNewMobile();
        String captcha = vo.getCaptcha();

        SmsRecord smsRecord = smsRecordMapper.selectFirst(newMobile, captcha);
        Assert.notNull(smsRecord, "验证码不正确");
        Assert.isTrue(smsRecord.getExpireTime().after(new Date()) && smsRecord.getUsed().equals(0), "验证码已过期");
        Assert.isTrue(mobile.equals(user.getMobile()), "旧手机号不正确");

        SysUserUpdateReqVO updateVO = new SysUserUpdateReqVO();
        updateVO.setId(userId);
        updateVO.setMobile(newMobile);
        updateUser(updateVO);

        smsRecord.setUsed(1);
        smsRecordMapper.updateById(smsRecord);

        // 同步学生/家长表数据
        boolean isStudent = user.getPresetRoleCode().equals(RoleEnum.student.getValue());
        if (isStudent) {
            StudentBaseInfo querySbi = studentMapper.selectOne(new QueryWrapper<StudentBaseInfo>().eq("user_id", userId));
            if (Objects.nonNull(querySbi)) {
                // h5端学生修改姓名同步到studentBaseInfo表
                StudentBaseInfo baseInfo = new StudentBaseInfo();
                baseInfo.setId(querySbi.getId());
                baseInfo.setMobile(newMobile);
                studentMapper.updateBatch(Collections.singletonList(baseInfo), key);
            }
        } else {
            // h5端家长同步修改parent表
            Parent queryParent = parentMapper.selectOne(new QueryWrapper<Parent>().eq("user_id", userId));
            if (Objects.nonNull(queryParent)) {
                Parent update = new Parent();
                update.setId(queryParent.getId());
                update.setMobile(newMobile);
                parentMapper.updateBatch(Collections.singletonList(update), key);
            }
        }

        // 手机号修改后修改学生的短id
        if (user.getPresetRoleCode().contains(RoleEnum.parents.getValue())) {
            List<Long> childrenUserIds = baseMapper.selectChildrenUserIdsByParentUserId(userId);
            for (Long childrenUserId : childrenUserIds) {
                SysUser updateUser = baseMapper.selectOneById(key, childrenUserId);
                if (Objects.isNull(updateUser)) {
                    continue;
                }

                String shortId = updateUser.getShortId();
                if (StrUtil.isNotEmpty(shortId) && shortId.contains(mobile)) {
                    shortId = shortId.replace(mobile, newMobile);

                    SysUser sysUser = new SysUser();
                    sysUser.setId(updateUser.getId());
                    sysUser.setShortId(shortId);
                    sysUser.setUpdateBy(userId);
                    sysUser.setUpdateTime(new Date());
                    baseMapper.updateShortId(sysUser, key);
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void h5UpdateUserInfo(SysUserReqVO vo) throws Exception {
        Long userId = SysUserUtil.getHeaderUserId();
        SysUserDTO userInfo = findByUserId(userId, false);
        boolean isStudent = userInfo.getPresetRoleCode().equals(RoleEnum.student.getValue());

        SysUserUpdateReqVO updateVO = new SysUserUpdateReqVO();
        updateVO.setId(userId);

        boolean updateFlag = false;
        if (StrUtil.isNotEmpty(vo.getName())) {
            if (!vo.getName().equals(userInfo.getName()) && userInfo.getNameChange() > 0) {
                throw exception("用户姓名只能修改一次，不能再次修改");
            }
            updateVO.setNameChange(userInfo.getNameChange() + 1);
            updateVO.setName(vo.getName());
            updateFlag = true;
        }

        if (vo.getSex() != null && vo.getSex() > -1) {
            Assert.isTrue(!isStudent, "学生账号不能进行性别修改");
            updateVO.setSex(vo.getSex());
            updateFlag = true;
        }

        Assert.isTrue(updateFlag, "无可用更改");

        updateUser(updateVO);

        if (isStudent) {
            StudentBaseInfo querySbi = studentMapper.selectOne(new QueryWrapper<StudentBaseInfo>().eq("user_id", userId));
            if (Objects.nonNull(querySbi)) {
                // h5端学生修改姓名同步到studentBaseInfo表
                StudentBaseInfo baseInfo = new StudentBaseInfo();
                baseInfo.setId(querySbi.getId());
                baseInfo.setName(vo.getName());
                studentMapper.updateBatch(Collections.singletonList(baseInfo), key);
            }
        } else {
            // h5端家长同步修改parent表
            Parent queryParent = parentMapper.selectOne(new QueryWrapper<Parent>().eq("user_id", userId));
            if (Objects.nonNull(queryParent)) {
                Parent update = new Parent();
                update.setId(queryParent.getId());
                update.setName(vo.getName());
                update.setSex(vo.getSex());
                parentMapper.updateBatch(Collections.singletonList(update), key);
            }
        }
    }

    @Override
    public void updateUserInfo(SysUserReqVO vo) {
        Assert.notNull(vo, "邮箱不能为空");
        Assert.notEmpty(vo.getEmail(), "邮箱不能为空");

        Long userId = UserUtil.getUserId();
        SysUser user = selectSysUserById(userId);
        Assert.notNull(user, USER_NOT_EXISTS.getMessage());

        //更新用户邮箱
        LambdaUpdateWrapper<SysUser> updateUserWrapper = new LambdaUpdateWrapper<>();
        updateUserWrapper.eq(SysUser::getId, userId).set(SysUser::getEmail, vo.getEmail()).set(SysUser::getUpdateBy, userId).set(SysUser::getUpdateTime, new Date());
        baseMapper.update(null, updateUserWrapper);

        cacheUser(userId);
    }

    @Override
    public CommonResult<List<String>> h5FileUpload(MultipartFile[] files) {
        Long userId = SysUserUtil.getHeaderUserId();
        SysUserDTO userInfo = findByUserId(userId, false);
        // 只有家长学生拥有操作此接口权限
        String roleCode = userInfo.getPresetRoleCode();
        Assert.isTrue(roleCode.equals(RoleEnum.student.getValue()) || roleCode.equals(RoleEnum.parents.getValue()), "用户角色不正确");

        return fileFeign.uploadFileToOss(new FileFeignUploadDTO(files, "true"));
    }

    @Override
    public void deleteBySchoolId(Long schoolId) {
        Set<Long> userIds = baseMapper.getUserIdBySchool(schoolId);
        delUser(userIds);
    }

    @Override
    public void deleteByAreaCode(Integer areaCode) {
        Set<Long> userIds = baseMapper.getUserIdByAreaCode(areaCode);
        delUser(userIds);
    }

    @Override
    public List<SchoolDTO> findSchoolByTestManager() {
        SysUserDTO user = UserUtil.getCurrentUser();
        String roleCode = user.getPresetRoleCode();
        Assert.isTrue(RoleEnum.test_admin.getValue().equals(roleCode), OPERATION_NOT_ALLOWED.getMessage());
        return testManagerMapper.findSchoolList(user.getId(), ApproveStatus.APPROVED.getCode());
    }

    @Override
    public String updateHeadImg(String headImgUrl) {
        Assert.notEmpty(headImgUrl, "缺失头像路径");
        SysUserDTO user = UserUtil.getCurrentUser();
        Assert.notNull(user, "用户信息未找到");

        Long userId = user.getId();
        SysUser sysUser = new SysUser();
        sysUser.setId(userId);

        if (headImgUrl.contains("mental-health-oss")) {
            String filePath = fileFeign.persistence(headImgUrl).getData();
            Assert.notEmpty(filePath, "持久化头像文件失败！");
            sysUser.setHeadImgUrl(filePath);
        } else {
            sysUser.setHeadImgUrl(headImgUrl);
        }

        baseMapper.updateById(sysUser);
        cacheUser(userId);

        return sysUser.getHeadImgUrl();
    }

    @Override
    public String getHeadImgUrlById(Long userId) {
        SysUser sysUser = baseMapper.selectById(userId);
        return sysUser == null ? "" : sysUser.getHeadImgUrl();
    }

    @Override
    public UpgradeDTO getPadUpgradeUrl(Integer currentVersion) {
        UpgradeDTO upgrade = null;
        String upgradeRes = baseMapper.getPadUpgradeUrl(currentVersion);
        if (StringUtils.isNotBlank(upgradeRes)) {
            String[] arr = upgradeRes.split("#");
            upgrade = new UpgradeDTO();
            upgrade.setVersion(Integer.parseInt(arr[0]));
            upgrade.setDownloadUrl(arr[1]);
            upgrade.setForceUpgrade(Integer.parseInt(arr[2]) > 0 ? Boolean.TRUE : Boolean.FALSE);
            upgrade.setUpgradeDescription(arr[3]);
        }
        return upgrade;
    }

    @Override
    public String getResourcePlatformCode() throws Exception {
        SysUserDTO user = UserUtil.getCurrentUser();
        Assert.notNull(user, "用户信息未找到");

        UserInfo userInfo = new UserInfo();
        userInfo.setPhone(user.getMobile());
        String presetRoleCode = user.getPresetRoleCode();
        userInfo.setRoleCode(presetRoleCode);
        if (RoleEnum.student.getValue().equals(presetRoleCode)) {
            StudentBaseInfoDTO student = studentMapper.getStudentListByUserId(user.getId(), key);
            Assert.notNull(student, "获取学生信息异常");
            userInfo.setGrade(student.getGradeName());
            userInfo.setIdno(user.getUsername());
            userInfo.setRole(UserInfo.ROLE_STUDENT);
        } else if (RoleEnum.parents.getValue().equals(presetRoleCode)) {
            userInfo.setRole(UserInfo.ROLE_PARENT);
        } else if (RoleEnum.school_psycho_teacher.getValue().equals(presetRoleCode)) {
            userInfo.setRole(UserInfo.ROLE_PSYCHO_TEACHER);
        } else if (RoleEnum.school_head_teacher.getValue().equals(presetRoleCode)) {
            userInfo.setRole(UserInfo.ROLE_HEAD_TEACHER);
        } else if (RoleEnum.school_staff.getValue().equals(presetRoleCode)) {
            userInfo.setRole(UserInfo.ROLE_STAFF);
        } else if (RoleEnum.school_leader.getValue().equals(presetRoleCode)) {
            userInfo.setRole(UserInfo.ROLE_SCHOOL_LEADER);
        } else {
            throw exception("该角色无权限");
        }
        userInfo.setName(user.getName());
        userInfo.setOrg(user.getSchoolName());
        return remoteRestAPI.getUserCode(userInfo);
    }

    @Override
    public Map<String, List<Map<String, Object>>> getUserStatistics() {
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int year = cal.get(Calendar.YEAR);
        String date = year + "-01";
        result.put("areaManager", baseMapper.getAreaManagerStatistics(date));
        result.put("schoolManager", baseMapper.getSchoolManagerStatistics(date));
        return result;
    }

    @Override
    public Integer getUserCountBySchoolId(Long schoolId, Integer userRoleType) {
        if (userRoleType.equals(0)) {
            Integer studentNum = studentMapper.selectCount(new QueryWrapper<StudentBaseInfo>().eq("school_id", schoolId));
            return studentNum == null ? 0 : studentNum;
        } else if (userRoleType.equals(1)) {
            Integer parentNum = parentMapper.selectCount(new QueryWrapper<Parent>().eq("school_id", schoolId));
            return parentNum == null ? 0 : parentNum;
        } else {
            Integer staffNum = schoolStaffMapper.selectCount(new QueryWrapper<SchoolStaff>().eq("school_id", schoolId));
            Integer teacherNum = schoolTeacherMapper.selectCount(new QueryWrapper<SchoolTeacher>().eq("school_id", schoolId));
            return (staffNum == null ? 0 : staffNum) + (teacherNum == null ? 0 : teacherNum);
        }
    }

    @Override
    public Integer getUserCountByAreaCode(String areaCode, Integer userRoleType) {
        return baseMapper.getAreaUserCount(areaCode, userRoleType);
    }

    @Override
    public Map<String, List<Map<String, Object>>> getTotalUserNumOfSchool(Long schoolId) {
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        List<Map<String, Object>> student = baseMapper.getGradesStudentNum(schoolId);
        List<Map<String, Object>> parent = baseMapper.getGradesParentNum(schoolId);
        List<Map<String, Object>> staff = baseMapper.getDepartmentStaffNum(schoolId);
        staff = staff == null ? new ArrayList<>() : staff;
        staff.addAll(baseMapper.getSchoolTeacherNum(schoolId));
        result.put("student", student == null ? new ArrayList<>() : student);
        result.put("parent", parent == null ? new ArrayList<>() : parent);
        result.put("staff", staff);
        return result;
    }

    @Override
    public Map<String, List<Map<String, Object>>> getTotalUserNumOfArea(String areaCode, Long wholeAreaCode) {
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        List<Map<String, Object>> student = baseMapper.getAreaStudentNum(areaCode);
        List<Map<String, Object>> parent = baseMapper.getAreaParentNum(areaCode);
        List<Map<String, Object>> schoolStaff = baseMapper.getAreaSchoolStaffNum(areaCode);
        List<Map<String, Object>> areaStaff = baseMapper.getAreaStaffNum(wholeAreaCode);
        List<Map<String, Object>> areaTeacher = baseMapper.getAreaTeacherNum(wholeAreaCode);
        fillDefault(areaStaff);
        fillDefault(areaTeacher);
        areaStaff = areaStaff == null ? new ArrayList<>() : areaStaff;
        areaTeacher = areaTeacher == null ? new ArrayList<>() : areaTeacher;
        areaStaff.addAll(areaTeacher);
        result.put("student", student == null ? new ArrayList<>() : student);
        result.put("parent", parent == null ? new ArrayList<>() : parent);
        result.put("schoolStaff", schoolStaff == null ? new ArrayList<>() : schoolStaff);
        result.put("areaStaff", areaStaff);
        return result;
    }

    private void fillDefault(List<Map<String, Object>> staffList) {
        if (CollUtil.isNotEmpty(staffList)) {
            for (Map<String, Object> areas : staffList) {
                if (areas.get("school_id") == null || StringUtils.isEmpty(areas.get("school_id").toString())) {
                    areas.put("school_id", "其他");
                }
                if (areas.get("name") == null || StringUtils.isEmpty(areas.get("name").toString())) {
                    areas.put("name", "其他");
                }
            }
        }
    }

    @Override
    public String findNameById(Long userId) {
        return baseMapper.findNameById(userId, key);
    }

    @Override
    public List<CommonDTO> findNameByIds(Set<Long> userIds) {
        return baseMapper.findNameByIds(userIds, key);
    }

    @Override
    public List<Long> getAreaUserIdByParams(Map<String, Object> params) {
        return baseMapper.getAreaUserIdByParams(params, key);
    }

    @Override
    public List<Long> findUserIdListByName(String name, List<String> roleCodes) {
        return baseMapper.findUserIdListByName(name, roleCodes, key);
    }

    @Override
    public List<Long> getUserIdsByQuery(Map<String, Object> params) {
        List<String> queryRoleEnumValue = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(params.get("userRoleType"))) {
            Integer userRoleType = Integer.parseInt(params.get("userRoleType").toString());
            queryRoleEnumValue = RoleEnum.getValueByTaskRole(userRoleType);
        }
        List<String> authRoleEnumValue = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(params.get("roleTypes"))) {
            String roleTypes = (String)params.get("roleTypes");
            String[] roleTypeArr = roleTypes.split(",");
            for (String role : roleTypeArr) {
                authRoleEnumValue.addAll(RoleEnum.getValueByTaskRole(Integer.parseInt(role)));
            }
        }
        params.put("queryRoleEnumValue", queryRoleEnumValue);
        params.put("authRoleEnumValue", authRoleEnumValue);
        return baseMapper.getUserIdsByQuery(params, key);
    }

}
