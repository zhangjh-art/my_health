package com.cnasoft.health.userservice.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.constant.RocketMQConstant;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.ClazzDTO;
import com.cnasoft.health.common.dto.ParentDTO;
import com.cnasoft.health.common.dto.ParentStudentDTO;
import com.cnasoft.health.common.dto.SchoolDTO;
import com.cnasoft.health.common.dto.SysUserDTO;
import com.cnasoft.health.common.dto.TransactionMsgDefinationDTO;
import com.cnasoft.health.common.encryptor.DesensitizedUtil;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.enums.RoleEnum;
import com.cnasoft.health.common.exception.constant.GlobalErrorCodeConstants;
import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.common.util.JsonUtils;
import com.cnasoft.health.common.util.SysUserUtil;
import com.cnasoft.health.common.util.text.TextValidator;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.constant.ShortId;
import com.cnasoft.health.userservice.convert.SysUserConvert;
import com.cnasoft.health.userservice.feign.TaskFeign;
import com.cnasoft.health.userservice.feign.dto.BindStudentVO;
import com.cnasoft.health.userservice.feign.dto.ParentBaseRespVO;
import com.cnasoft.health.userservice.feign.dto.ParentBindStudentVO;
import com.cnasoft.health.userservice.feign.dto.ParentReqVO;
import com.cnasoft.health.userservice.feign.dto.ParentRespVO;
import com.cnasoft.health.userservice.feign.dto.StudentRespVO;
import com.cnasoft.health.userservice.feign.dto.StudentVO;
import com.cnasoft.health.userservice.mapper.ParentMapper;
import com.cnasoft.health.userservice.mapper.RocketmqTransactionLogMapper;
import com.cnasoft.health.userservice.mapper.SmsRecordMapper;
import com.cnasoft.health.userservice.mapper.StudentBaseInfoMapper;
import com.cnasoft.health.userservice.mapper.SysUserMapper;
import com.cnasoft.health.userservice.model.Parent;
import com.cnasoft.health.userservice.model.RocketmqTransactionLog;
import com.cnasoft.health.userservice.model.SmsRecord;
import com.cnasoft.health.userservice.model.StudentBaseInfo;
import com.cnasoft.health.userservice.model.SysUser;
import com.cnasoft.health.userservice.service.IClazzService;
import com.cnasoft.health.userservice.service.IParentService;
import com.cnasoft.health.userservice.service.ISmsRecordService;
import com.cnasoft.health.userservice.service.ISysUserService;
import com.cnasoft.health.userservice.util.DataCacheUtil;
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
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.cnasoft.health.common.exception.util.ServiceExceptionUtil.exception;

/**
 * @author Administrator
 */
@Service
@Slf4j
public class ParentServiceImpl extends SuperServiceImpl<ParentMapper, Parent> implements IParentService {
    @Value("${user.password.key}")
    private String key;
    @Resource
    private ISysUserService userService;
    @Resource
    private StudentBaseInfoMapper studentMapper;
    @Resource
    private SysUserMapper userMapper;
    @Resource
    private TaskFeign taskFeign;
    @Resource
    private SmsRecordMapper smsRecordMapper;
    @Resource
    private IClazzService clazzService;
    @Resource
    private ISmsRecordService smsRecordService;
    @Resource
    RocketMQTemplate rocketMQTemplate;
    @Resource
    RocketmqTransactionLogMapper rocketmqTransactionLogMapper;

    /**
     * 新增家长
     *
     * @param parentVO 请求数据
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<String> create(ParentReqVO parentVO) throws Exception {
        List<String> errorList = new ArrayList<>();
        Long schoolId = UserUtil.getSchoolId();

        //插入sys_user
        SysUser sysUser = SysUserConvert.INSTANCE.convertVO(parentVO);
        sysUser.setSchoolId(schoolId);
        sysUser.setApproveStatus(ApproveStatus.APPROVED.getCode());
        boolean result = userService.saveUserPublic(sysUser, RoleEnum.parents);
        if (result) {
            Long userId = sysUser.getId();

            //保存家长
            Parent parent = SysUserConvert.INSTANCE.convertParentVO(parentVO);
            parent.setUserId(userId);
            parent.setSchoolId(schoolId);
            parent.setIsActive(false);
            parent.setConfirmed(false);
            parent.setUsername(parentVO.getMobile());
            parent.setEnabled(true);
            baseMapper.insertBatch(Collections.singletonList(parent), key);

            //如果填写了学生id，建立关联关系  传递过来的学生身份证集合
            List<StudentVO> idCardNumbers = parentVO.getStudents();
            if (CollectionUtils.isNotEmpty(idCardNumbers)) {
                for (StudentVO studentVO : idCardNumbers) {
                    String idCardNumber = studentVO.getIdentityCardNumber();
                    Integer relationship = studentVO.getRelationship();

                    if (StrUtil.isEmpty(idCardNumber) || ObjectUtils.isEmpty(relationship)) {
                        continue;
                    }

                    Map<String, Object> params = new HashMap<>(16);
                    params.put("identityCardNumber", idCardNumber);
                    params.put("schoolId", schoolId);

                    StudentBaseInfo studentBaseInfo = studentMapper.selectOneByParams(params, key);
                    if (ObjectUtils.isNotEmpty(studentBaseInfo)) {

                        SysUser user = new SysUser();
                        user.setId(studentBaseInfo.getUserId());
                        user.setShortId(generateShortId(parent.getId(), parent.getMobile()));
                        userMapper.updateBatch(Collections.singletonList(user), key);

                        LambdaUpdateWrapper<StudentBaseInfo> updateWrapper = new LambdaUpdateWrapper<>();
                        updateWrapper.eq(StudentBaseInfo::getId, studentBaseInfo.getId()).set(StudentBaseInfo::getParentId, parent.getId())
                            .set(StudentBaseInfo::getRelationship, relationship);
                        studentMapper.update(null, updateWrapper);

                    } else {
                        errorList.add(DesensitizedUtil.desensitized(idCardNumber, DesensitizedUtil.DesensitizedType.ID_CARD) + "不存在该学生信息");
                    }
                }
            }
        }

        return errorList;
    }

    /**
     * 生成学生短id
     *
     * @param parentId     家长id
     * @param parentMobile 家长手机号
     * @return 短id
     */
    private String generateShortId(Long parentId, String parentMobile) {
        int parentBoundStudentCount = studentMapper.selectCount(new QueryWrapper<StudentBaseInfo>().lambda().eq(StudentBaseInfo::getParentId, parentId));
        if (parentBoundStudentCount >= ShortId.getShortIdSuffix().length) {
            throw exception("一个家长最多绑定10个学生");
        }
        return parentMobile + ShortId.getShortIdSuffix()[parentBoundStudentCount];
    }

    @Override
    public List<String> updateParent(ParentReqVO parentVO) throws Exception {
        Long schoolId = UserUtil.getSchoolId();

        List<String> errorList = new ArrayList<>();
        Parent oldParent = parentExists(parentVO.getId(), schoolId);
        Assert.notNull(oldParent, GlobalErrorCodeConstants.DATE_NOT_EXIST.getMessage());

        //更新用户信息
        SysUser user = SysUserConvert.INSTANCE.convertVO(parentVO);
        user.setId(oldParent.getUserId());
        user.setUsername(StrUtil.isEmpty(parentVO.getMobile()) ? oldParent.getUsername() : parentVO.getMobile());
        userService.updateUserPublic(user);

        //更新家长
        Parent parent = SysUserConvert.INSTANCE.convertParentVO(parentVO);
        String mobile = parent.getMobile();
        if (StrUtil.isEmpty(mobile)) {
            mobile = oldParent.getMobile();
        }

        //清空该家长已绑定学生的短id
        List<Long> studentUserIds = baseMapper.getChildrenUserId(parent.getId(), key);
        if (CollUtil.isNotEmpty(studentUserIds)) {
            LambdaUpdateWrapper<SysUser> userUpdateWrapper = new LambdaUpdateWrapper<>();
            userUpdateWrapper.in(SysUser::getId, studentUserIds).set(SysUser::getShortId, null);
            userService.update(null, userUpdateWrapper);
        }

        //解绑之前的家长、学生绑定关系
        LambdaUpdateWrapper<StudentBaseInfo> unbindWrapper = new LambdaUpdateWrapper<>();
        unbindWrapper.eq(StudentBaseInfo::getParentId, parent.getId()).set(StudentBaseInfo::getParentId, null).set(StudentBaseInfo::getRelationship, null);
        studentMapper.update(null, unbindWrapper);

        //如果填写了学生id，建立关联关系  传递过来的学生身份证集合
        List<StudentVO> idCardNumbers = parentVO.getStudents();
        if (CollectionUtils.isNotEmpty(idCardNumbers)) {
            for (StudentVO studentVO : idCardNumbers) {
                Long userId = studentVO.getUserId();
                String idCardNumber = studentVO.getIdentityCardNumber();
                Integer relationship = studentVO.getRelationship();

                if (Objects.isNull(relationship)) {
                    errorList.add("家长关系不能为空");
                    continue;
                }

                Map<String, Object> params = new HashMap<>(16);
                StudentBaseInfo studentBaseInfo;
                if (Objects.nonNull(userId)) {
                    if (StringUtils.isNotBlank(idCardNumber)) {
                        params.put("identityCardNumber", idCardNumber);
                        params.put("schoolId", schoolId);
                    } else {
                        params.put("userId", userId);
                    }
                } else {
                    if (StringUtils.isBlank(idCardNumber)) {
                        errorList.add("身份证号不能为空");
                        continue;
                    }
                    params.put("identityCardNumber", idCardNumber);
                    params.put("schoolId", schoolId);
                }

                studentBaseInfo = studentMapper.selectOneByParams(params, key);
                if (Objects.isNull(studentBaseInfo)) {
                    errorList.add(DesensitizedUtil.desensitized(idCardNumber, DesensitizedUtil.DesensitizedType.ID_CARD) + "不存在该学生信息");
                } else {
                    //更新学生用户表的短id
                    SysUser sysUser = new SysUser();
                    sysUser.setId(studentBaseInfo.getUserId());
                    sysUser.setShortId(generateShortId(parent.getId(), mobile));
                    userMapper.updateBatch(Collections.singletonList(sysUser), key);

                    LambdaUpdateWrapper<StudentBaseInfo> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(StudentBaseInfo::getId, studentBaseInfo.getId()).set(StudentBaseInfo::getParentId, parent.getId())
                        .set(StudentBaseInfo::getRelationship, relationship);
                    studentMapper.update(null, updateWrapper);
                }
            }
        }

        baseMapper.updateBatch(Collections.singletonList(parent), key);

        //缓存用户数据
        userService.cacheUser(user.getId());
        return errorList;
    }

    @Override
    public List<BatchOperationTipDTO> delete(Set<Long> ids) {
        List<BatchOperationTipDTO> resultMap = new ArrayList<>();
        Set<Long> delIds = new HashSet<>();

        if (CollectionUtils.isNotEmpty(ids)) {
            Long schoolId = UserUtil.getSchoolId();
            for (Long id : ids) {
                Parent parent = parentExists(id, schoolId);
                if (Objects.isNull(parent)) {
                    resultMap.add(new BatchOperationTipDTO(id, "无效id"));
                    continue;
                }

                // 删除家长数据
                baseMapper.deleteById(id);

                // 清空学生表该家长的id
                LambdaUpdateWrapper<StudentBaseInfo> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(StudentBaseInfo::getParentId, id).set(StudentBaseInfo::getParentId, null).set(StudentBaseInfo::getRelationship, null);
                studentMapper.update(null, updateWrapper);

                delIds.add(parent.getUserId());
            }
            // 删除用户表数据
            userService.delUser(ids);
        }

        return resultMap;
    }

    /**
     * 校管理员/班主任/校心理老师->家长列表
     *
     * @param params 查询条件
     * @return TaskUserResVO
     */
    @Override
    public PageResult<ParentRespVO> list(Map<String, Object> params) {
        Page<ParentRespVO> page = new Page<>(MapUtil.getInt(params, Constant.PAGE_NUM, 1), MapUtil.getInt(params, Constant.PAGE_SIZE, 10));
        UserUtil.setSearchParams(params);

        if (params.containsKey(Constant.ENABLED)) {
            params.put(Constant.ENABLED, Boolean.valueOf(params.get(Constant.ENABLED).toString()));
        }

        if (params.containsKey(Constant.ACTIVE)) {
            params.put(Constant.ACTIVE, Boolean.valueOf(params.get(Constant.ACTIVE).toString()));
        }

        long total;
        //获取当前用户信息
        SysUserDTO sysUserDto = UserUtil.getCurrentUser();
        //当前登录人角色编码
        String roleCode = sysUserDto.getPresetRoleCode();
        Long schoolId = 0L;
        //获取当前用户的学校id
        if (RoleEnum.school_psycho_teacher.getValue().equals(roleCode) || RoleEnum.school_admin.getValue().equals(roleCode) || RoleEnum.school_head_teacher.getValue()
            .equals(roleCode)) {
            schoolId = UserUtil.getSchoolId();
        }

        params.put("schoolId", schoolId);

        //当前用户角色为班主任
        if (RoleEnum.school_head_teacher.getValue().equals(roleCode)) {
            //查询班主任管理的班级信息
            List<ClazzDTO> clazzList = clazzService.getListByHeaderTeacher();
            if (CollectionUtils.isNotEmpty(clazzList)) {
                params.put("clazzIds", clazzList.stream().map(ClazzDTO::getId).collect(Collectors.toList()));
                params.put("createBy", sysUserDto.getId());
            }
        }

        String query = MapUtil.getStr(params, "query", "");
        if (StringUtils.isNotBlank(query)) {
            if (TextValidator.isMobileExact(query)) {
                params.put("mobile", query);
                params.put("query", null);
            }
        }

        Long clazzId = MapUtil.getLong(params, "clazzId", 0L);
        if (clazzId > 0) {
            params.put("clazzIds", null);
        }

        page.setSearchCount(false);
        List<ParentRespVO> parents = baseMapper.pageList(page, params, key);
        total = baseMapper.pageListCount(params, key);

        if (!parents.isEmpty()) {

            //获取学校年级数据
            Map<String, String> dictMap = UserUtil.getDictData(UserUtil.SCHOOL_GRADE);

            // 敏感数据脱敏
            parents.forEach(parent -> {
                String mobile = parent.getMobile();
                if (StringUtils.isNotBlank(mobile)) {
                    parent.setMobile(DesensitizedUtil.desensitized(mobile, DesensitizedUtil.DesensitizedType.MOBILE_PHONE));
                }

                List<StudentRespVO> students = parent.getStudents();
                if (CollUtil.isNotEmpty(students)) {
                    students = students.stream().filter(r -> r.getIsDeleted().equals(Boolean.FALSE) && r.getClazzDeleted().equals(Boolean.FALSE)).collect(Collectors.toList());
                    students.forEach(student -> {
                        String idCardNumber = student.getIdentityCardNumber();
                        if (StringUtils.isNotBlank(idCardNumber)) {
                            student.setIdentityCardNumber(DesensitizedUtil.desensitized(idCardNumber, DesensitizedUtil.DesensitizedType.ID_CARD));
                        }

                        if (dictMap.containsValue(student.getGrade())) {
                            String gradeValue = UserUtil.getKey(dictMap, student.getGrade());
                            if (StringUtils.isNotBlank(gradeValue)) {
                                student.setGrade(gradeValue);
                            }
                        }
                    });
                    parent.setStudents(students);
                }
            });
        }

        return PageResult.<ParentRespVO>builder().data(parents).count(total).build();
    }

    /**
     * 家长pc端通过学生姓名和生日绑定学生
     *
     * @param query 请求数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> pcBindStudents(ParentBindStudentVO query) {
        Long userId = SysUserUtil.getHeaderUserId();

        return bindStudents(query, userId);
    }

    /**
     * 家长移动端通过学生姓名身份证绑定学生
     *
     * @param query 请求数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> mobileBindStudents(ParentBindStudentVO query) {
        //根据手机号、验证码获取当前操作人userId
        SmsRecord smsRecord = smsRecordMapper.selectFirst(query.getM(), query.getV());
        if (Objects.isNull(smsRecord)) {
            throw exception("验证码错误");
        }
        if (smsRecord.getUsed() == 1) {
            throw exception("验证码已使用");
        }
        if (new Date().after(smsRecord.getExpireTime())) {
            throw exception("验证码已失效");
        }

        Long userId = smsRecord.getUserId();
        //查询家长
        Parent parent = baseMapper.selectOne(new LambdaQueryWrapper<Parent>().eq(Parent::getUserId, userId));
        if (null == parent) {
            throw exception("没有家长数据");
        }

        //绑定结果
        List<String> bindResult = bindStudents(query, userId);

        smsRecord.setUsed(1);
        smsRecord.setUpdateTime(new Date());
        smsRecordMapper.updateById(smsRecord);

        return bindResult;
    }

    /**
     * 绑定学生
     *
     * @param query  请求参数
     * @param userId 家长的sysUserId
     */
    private List<String> bindStudents(ParentBindStudentVO query, Long userId) {
        SysUserDTO sysUser = userService.findByUserId(userId, false);
        String roleCode = sysUser.getPresetRoleCode();

        if (!RoleEnum.parents.getValue().equals(roleCode)) {
            throw exception("只有家长才可以绑定学生！");
        }

        Map<String, Object> params = new HashMap<>(2);
        params.put("userId", userId);
        Parent parent = baseMapper.selectOneByParams(params, key);
        if (parent == null) {
            throw exception("家长不存在");
        }

        String parentMobile = parent.getMobile();
        Assert.notNull(parentMobile, "家长手机号为空");

        List<String> errorList = new ArrayList<>();
        List<BindStudentVO> bindStudents = query.getStudents();
        Long parentId = parent.getId();

        //根据条件查询学生数据
        List<StudentBaseInfo> students = studentMapper.getStudent(bindStudents, key);

        //输入多个学生，有的未找到，有的能找到的处理
        if (CollectionUtils.isEmpty(students)) {
            throw exception("学生信息不存在");
        }

        //筛选并移除不存在的学生数据
        List<String> idCardNumbers = students.stream().map(StudentBaseInfo::getIdentityCardNumber).collect(Collectors.toList());
        List<BindStudentVO> temp = new ArrayList<>();
        for (BindStudentVO bindStudent : bindStudents) {
            String idCardNumber = bindStudent.getIdentityCardNumber();
            if (!idCardNumbers.contains(idCardNumber)) {
                errorList.add(DesensitizedUtil.desensitized(idCardNumber, DesensitizedUtil.DesensitizedType.ID_CARD) + "系统中不存在学生数据");
                temp.add(bindStudent);
            }
        }
        if (!temp.isEmpty()) {
            bindStudents.removeAll(temp);
        }

        //获取已绑定了其他家长的学生数据并移除
        List<StudentBaseInfo> studentsHaveOtherParent =
            students.stream().filter(s -> Objects.nonNull(s.getParentId()) && s.getParentId() != 0 && !s.getParentId().equals(parentId)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(studentsHaveOtherParent)) {
            idCardNumbers = studentsHaveOtherParent.stream().map(StudentBaseInfo::getIdentityCardNumber).collect(Collectors.toList());
            temp = new ArrayList<>();

            for (BindStudentVO bindStudent : bindStudents) {
                String idCardNumber = bindStudent.getIdentityCardNumber();
                if (idCardNumbers.contains(idCardNumber)) {
                    errorList.add(DesensitizedUtil.desensitized(idCardNumber, DesensitizedUtil.DesensitizedType.ID_CARD) + "已绑定其他家长");
                    temp.add(bindStudent);
                }
            }
            if (!temp.isEmpty()) {
                bindStudents.removeAll(temp);
            }
        }

        if (!bindStudents.isEmpty()) {
            if (Boolean.FALSE.equals(parent.getIsActive())) {
                //修改家长为激活、确认状态
                LambdaUpdateWrapper<Parent> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(Parent::getId, parent.getId()).set(Parent::getConfirmed, true).set(Parent::getIsActive, true);
                baseMapper.update(null, updateWrapper);
            }

            //查询该家长已绑定的学生个数
            int parentBoundStudentCount = studentMapper.selectCount(new LambdaQueryWrapper<StudentBaseInfo>().eq(StudentBaseInfo::getParentId, parentId));
            if ((parentBoundStudentCount + bindStudents.size()) >= ShortId.getShortIdSuffix().length) {
                throw exception("一个家长最多绑定10个学生");
            }

            Map<String, Integer> parentRelationshipMap =
                bindStudents.stream().collect(Collectors.toMap(BindStudentVO::getIdentityCardNumber, BindStudentVO::getRelationship, (key1, key2) -> key2));

            List<StudentBaseInfo> studentNotParents = new ArrayList<>();
            for (int i = 0; i < students.size(); i++) {
                StudentBaseInfo student = students.get(i);
                if (Objects.isNull(student.getParentId())) {
                    studentNotParents.add(student);

                    // 更新学生短id
                    SysUser user = new SysUser();
                    user.setId(student.getUserId());
                    user.setShortId(parentMobile + ShortId.getShortIdSuffix()[i + parentBoundStudentCount]);
                    user.setUpdateBy(userId);
                    user.setUpdateTime(new Date());
                    userMapper.updateShortId(user, key);

                    // 更新学生家长关系
                    LambdaUpdateWrapper<StudentBaseInfo> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(StudentBaseInfo::getId, student.getId()).set(StudentBaseInfo::getParentId, parentId)
                        .set(StudentBaseInfo::getRelationship, parentRelationshipMap.get(student.getIdentityCardNumber()));
                    studentMapper.update(null, updateWrapper);
                } else if (parentId.equals(student.getParentId())) {
                    //如果该学生之前绑定的家长与当前操作人是同一人，则更新绑定信息
                    // 更新学生家长关系
                    LambdaUpdateWrapper<StudentBaseInfo> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(StudentBaseInfo::getId, student.getId()).set(StudentBaseInfo::getRelationship, parentRelationshipMap.get(student.getIdentityCardNumber()));
                    studentMapper.update(null, updateWrapper);
                }
            }
        }

        return errorList;
    }

    @Override
    public void updateConfirmAndActiveStatus(Long userId) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("userId", userId);
        Parent parent = baseMapper.selectOneByParams(params, key);
        if (ObjectUtils.isEmpty(parent)) {
            log.error("更新家长{}确认状态及激活状态失败,家长数据不存在", userId);
        } else {
            LambdaUpdateWrapper<Parent> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Parent::getId, parent.getId()).set(Parent::getConfirmed, true).set(Parent::getIsActive, true);
            baseMapper.update(null, updateWrapper);

            //更新缓存数据
            userService.cacheUser(userId);
        }
    }

    @Override
    public ParentRespVO infoByUserId(Long userId) {
        Long schoolId = UserUtil.getSchoolId();
        ParentRespVO parent = baseMapper.findInfoByUserId(userId, schoolId, this.key);
        if (Objects.isNull(parent)) {
            return null;
        }
        fillAdditionalInfo(parent, userId);
        return parent;
    }

    @Override
    public ParentRespVO infoOnlyByUserId(Long userId) {
        ParentRespVO parent = baseMapper.findInfoByUserId(userId, null, this.key);
        if (Objects.isNull(parent)) {
            return null;
        }
        fillAdditionalInfo(parent, userId);
        return parent;
    }

    private void fillAdditionalInfo(ParentRespVO parent, Long userId) {
        // 敏感数据脱敏
        String mobile = parent.getMobile();
        if (StringUtils.isNotBlank(mobile)) {
            parent.setMobile(DesensitizedUtil.desensitized(mobile, DesensitizedUtil.DesensitizedType.MOBILE_PHONE));
        }
        SysUserDTO user = userService.findByUserId(userId, false);
        Assert.notNull(user, "获取用户失败");

        SysUserUtil.desensitizedUserInfo(user);
        parent.setNickname(user.getNickname());
        parent.setUsername(user.getUsername());
        parent.setEmail(user.getEmail());

        List<StudentRespVO> students = parent.getStudents();
        students.forEach(student -> {
            UserUtil.desensitizedMobile(student);
            String idCardNumber = student.getIdentityCardNumber();
            if (StringUtils.isNotBlank(idCardNumber)) {
                student.setIdentityCardNumber(DesensitizedUtil.desensitized(idCardNumber, DesensitizedUtil.DesensitizedType.ID_CARD));
            }
        });
    }

    @Override
    public void updateCurrentParent(ParentReqVO parentReqVO) throws Exception {
        Long schoolId = UserUtil.getSchoolId();
        Long userId = UserUtil.getUserId();
        ParentRespVO oldParent = baseMapper.findInfoByUserId(userId, schoolId, this.key);
        Assert.notNull(oldParent, GlobalErrorCodeConstants.DATE_NOT_EXIST.getMessage());

        if (StringUtils.isEmpty(parentReqVO.getVerifyCode())) {
            //未修改手机号
            parentReqVO.setMobile(null);
        } else {
            //校验手机号
            smsRecordService.checkCaptcha(parentReqVO.getMobile(), parentReqVO.getVerifyCode());
        }
        // 更新用户信息
        SysUser user = new SysUser();
        user.setId(oldParent.getUserId());
        user.setName(parentReqVO.getName());
        user.setEmail(parentReqVO.getEmail());
        user.setMobile(parentReqVO.getMobile());
        user.setNickname(parentReqVO.getNickname());
        userService.updateUserPublic(user);
        //更新缓存数据
        userService.cacheUser(userId);
        // 同步更新家长的邮箱
        if (parentReqVO.getName() != null || parentReqVO.getMobile() != null || parentReqVO.getEmail() != null) {
            Parent parent = new Parent();
            parent.setId(oldParent.getId());
            parent.setName(parentReqVO.getName());
            parent.setMobile(parentReqVO.getMobile());
            parent.setEmail(parentReqVO.getEmail());
            baseMapper.updateBatch(Collections.singletonList(parent), key);
        }

        if (CollectionUtils.isEmpty(parentReqVO.getStudents())) {
            return;
        }
        for (StudentVO student : parentReqVO.getStudents()) {
            StudentBaseInfo studentBaseInfo = studentMapper.selectById(student.getId());
            Assert.notNull(studentBaseInfo, "获取学生信息失败");
            Assert.notNull(studentBaseInfo.getUserId(), "获取学生信息失败");
            SysUser studentUser = new SysUser();
            studentUser.setId(studentBaseInfo.getUserId());
            studentUser.setName(student.getName());
            userService.updateUserPublic(studentUser);
            StudentBaseInfo baseInfo = new StudentBaseInfo();
            baseInfo.setId(studentBaseInfo.getId());
            baseInfo.setName(student.getName());
            baseInfo.setRelationship(student.getRelationship());
            studentMapper.updateStudentBaseInfo(baseInfo, key);
        }
    }

    /**
     * 根据id和学校id查看数据是否存在
     *
     * @param id       家长id
     * @param schoolId 学校id
     * @return 家长
     */
    public Parent parentExists(Long id, Long schoolId) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("id", id);
        params.put("schoolId", schoolId);
        return baseMapper.selectOneByParams(params, key);
    }

    @Override
    public List<String> h5BindStudent(BindStudentVO vo) {
        ParentBindStudentVO query = new ParentBindStudentVO();
        List<BindStudentVO> students = new ArrayList<>();
        students.add(vo);
        query.setStudents(students);

        Long userId = SysUserUtil.getHeaderUserId();
        return bindStudents(query, userId);
    }

    @Override
    public void deleteBySchoolId(Long schoolId) {
        LambdaQueryWrapper<Parent> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(Parent::getSchoolId, schoolId);
        baseMapper.delete(deleteWrapper);
    }

    @Override
    public List<Map<String, Object>> getSelectList(Long schoolId, String userName) {
        return baseMapper.getSelectList(schoolId, userName, key);
    }

    @Override
    public PageResult<ParentBaseRespVO> listBaseInfo(Map<String, Object> params) {
        Page<ParentRespVO> page = new Page<>(MapUtil.getInt(params, Constant.PAGE_NUM, 1), MapUtil.getInt(params, Constant.PAGE_SIZE, 10));
        params.put("schoolId", UserUtil.getSchoolId());
        List<ParentBaseRespVO> parents = baseMapper.listBaseInfo(page, params, key);
        parents.forEach(e -> {
            if (StringUtils.isNotBlank(e.getMobile())) {
                e.setMobile(DesensitizedUtil.desensitized(e.getMobile(), DesensitizedUtil.DesensitizedType.MOBILE_PHONE));
            }
            e.setHeadImgUrl(userService.getHeadImgUrlById(e.getUserId()));
        });

        return PageResult.<ParentBaseRespVO>builder().data(parents).count(page.getTotal()).build();
    }

    @Override
    public List<Long> getParentUserIdByParams(Map<String, Object> params) {
        return baseMapper.getParentUserIdByParams(params, key);
    }

    @Override
    public ParentStudentDTO findParentInfo(Long userId) {
        ParentStudentDTO parentInfo = baseMapper.findParentInfo(userId, key);
        if (Objects.nonNull(parentInfo) && parentInfo.isSchool()) {
            Long schoolId = parentInfo.getSchoolId();
            Map<Long, SchoolDTO> schoolMap = DataCacheUtil.getSchoolMap();
            if (schoolMap.containsKey(schoolId)) {
                parentInfo.setSchoolName(schoolMap.get(schoolId).getName());
            }
        }
        return parentInfo;
    }

    @Override
    public List<ParentDTO> findParentList(Set<Long> userIds) {
        List<ParentDTO> parentList = baseMapper.findParentList(userIds, key);
        if (CollectionUtils.isNotEmpty(parentList)) {
            Map<Long, SchoolDTO> schoolMap = DataCacheUtil.getSchoolMap();
            for (ParentDTO parentInfo : parentList) {
                if (parentInfo.isSchool()) {
                    Long schoolId = parentInfo.getSchoolId();
                    if (schoolMap.containsKey(schoolId)) {
                        parentInfo.setSchoolName(schoolMap.get(schoolId).getName());
                    }
                }
            }
        }
        return parentList;
    }
}