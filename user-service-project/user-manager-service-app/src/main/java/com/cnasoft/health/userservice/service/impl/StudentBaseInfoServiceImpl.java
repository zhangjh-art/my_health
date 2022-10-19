package com.cnasoft.health.userservice.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.ClazzDTO;
import com.cnasoft.health.common.dto.SchoolDTO;
import com.cnasoft.health.common.dto.StudentBaseInfoDTO;
import com.cnasoft.health.common.dto.StudentDTO;
import com.cnasoft.health.common.dto.SysUserDTO;
import com.cnasoft.health.common.encryptor.DesensitizedUtil;
import com.cnasoft.health.common.encryptor.EncryptorUtil;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.enums.RoleEnum;
import com.cnasoft.health.common.enums.StudentStatus;
import com.cnasoft.health.common.exception.constant.GlobalErrorCodeConstants;
import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.common.vo.UserClazzVO;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.constant.ShortId;
import com.cnasoft.health.userservice.constant.UserErrorCodeConstants;
import com.cnasoft.health.userservice.convert.SysUserConvert;
import com.cnasoft.health.userservice.feign.dto.StudentBaseInfoRespVO;
import com.cnasoft.health.userservice.feign.dto.StudentBaseReqVO;
import com.cnasoft.health.userservice.feign.dto.StudentBaseRespVO;
import com.cnasoft.health.userservice.feign.dto.StudentInfoRespVO;
import com.cnasoft.health.userservice.feign.dto.StudentRespVO;
import com.cnasoft.health.userservice.feign.dto.StudentSaveVO;
import com.cnasoft.health.userservice.mapper.ClazzMapper;
import com.cnasoft.health.userservice.mapper.ParentMapper;
import com.cnasoft.health.userservice.mapper.StudentAdditionalInfoMapper;
import com.cnasoft.health.userservice.mapper.StudentBaseInfoMapper;
import com.cnasoft.health.userservice.mapper.StudentFamilyConditionMapper;
import com.cnasoft.health.userservice.model.Clazz;
import com.cnasoft.health.userservice.model.Parent;
import com.cnasoft.health.userservice.model.StudentAdditionalInfo;
import com.cnasoft.health.userservice.model.StudentBaseInfo;
import com.cnasoft.health.userservice.model.StudentFamilyCondition;
import com.cnasoft.health.userservice.model.SysUser;
import com.cnasoft.health.userservice.service.IClazzService;
import com.cnasoft.health.userservice.service.IParentService;
import com.cnasoft.health.userservice.service.IStudentBaseInfoService;
import com.cnasoft.health.userservice.util.DataCacheUtil;
import com.cnasoft.health.userservice.util.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
public class StudentBaseInfoServiceImpl extends SuperServiceImpl<StudentBaseInfoMapper, StudentBaseInfo> implements IStudentBaseInfoService {

    @Value("${user.password.key}")
    private String key;
    @Resource
    private SysUserServiceImpl userService;
    @Resource
    private StudentAdditionalInfoMapper additionalInfoMapper;
    @Resource
    private StudentFamilyConditionMapper familyConditionMapper;
    @Resource
    private IClazzService clazzService;
    @Resource
    private ClazzMapper clazzMapper;
    @Resource
    private IParentService parentService;
    @Resource
    private ParentMapper parentMapper;

    /**
     * 新增学生
     *
     * @param student 请求数据
     * @return 学生基本信息
     * @throws Exception 异常信息
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public StudentBaseInfo create(StudentSaveVO student) throws Exception {
        Integer studentStatus = student.getStudentStatus();
        if (Objects.isNull(studentStatus)) {
            student.setStudentStatus(StudentStatus.NORMAL.getCode());
        } else {
            Assert.isFalse(StudentStatus.NONE.equals(StudentStatus.getStudentStatus(studentStatus)), "无效的学籍状态");
        }

        Long schoolId = UserUtil.getSchoolId();

        //保存用户
        String identityCardNumber = StringUtils.upperCase(student.getIdentityCardNumber());
        SysUser sysUser = SysUserConvert.INSTANCE.convertVO(student);
        sysUser.setUsername(identityCardNumber);
        sysUser.setPassword(EncryptorUtil.encrypt(StringUtils.upperCase(identityCardNumber).substring(identityCardNumber.length() - 6)));
        sysUser.setApproveStatus(ApproveStatus.APPROVED.getCode());
        sysUser.setSex(UserUtil.getSex(identityCardNumber));
        sysUser.setSchoolId(schoolId);

        boolean result = userService.saveUserPublic(sysUser, RoleEnum.student);
        if (result) {
            Long userId = sysUser.getId();

            //保存学生
            StudentBaseInfo baseInfo = SysUserConvert.INSTANCE.convertStudentVO(student);
            baseInfo.setUserId(userId);
            baseInfo.setUsername(identityCardNumber);
            baseInfo.setSex(UserUtil.getSex(identityCardNumber));

            baseInfo.setEnabled(true);
            baseInfo.setSchoolId(schoolId);
            //根据年级设置入学年份
            baseInfo.setAdmissionYear(UserUtil.getAdmissionYear(student.getGrade()));
            baseInfo.setBirthday(UserUtil.getBirthday(identityCardNumber));
            if (StringUtils.isNotBlank(student.getParentMobile())) {
                //查询家长
                Parent parent = parentMapper.findByMobile(student.getParentMobile(), schoolId, this.key);
                Assert.notNull(parent, UserErrorCodeConstants.PARENT_NOT_EXISTS.getMessage());
                baseInfo.setParentId(parent.getId());

                //更新学生用户表的短id
                SysUser user = new SysUser();
                user.setId(userId);
                user.setShortId(generateShortId(parent.getId(), student.getParentMobile()));
                user.setUpdateBy(userId);
                user.setUpdateTime(new Date());
                userService.getBaseMapper().updateShortId(user, key);
            }
            baseMapper.insertBatch(Collections.singletonList(baseInfo), key);

            Long studentId = baseInfo.getId();
            //保存学生补充信息
            StudentAdditionalInfo additionalInfo = SysUserConvert.INSTANCE.convertAdditionalVO(student);
            additionalInfo.setStudentId(studentId);
            additionalInfoMapper.insertBatch(Collections.singletonList(additionalInfo), key);

            //学生家庭情况表
            List<Integer> conditions = student.getFamilyConditions();
            if (CollectionUtils.isNotEmpty(conditions)) {
                List<StudentFamilyCondition> familyConditions = new ArrayList<>();
                for (Integer condition : conditions) {
                    StudentFamilyCondition studentFamilyCondition = new StudentFamilyCondition();
                    studentFamilyCondition.setStudentId(studentId);
                    studentFamilyCondition.setFamilyCondition(condition);
                    familyConditions.add(studentFamilyCondition);
                }
                familyConditionMapper.saveBatch(familyConditions);
            }
            return baseInfo;
        }
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateStudent(StudentSaveVO student) throws Exception {
        Long studentId = student.getId();
        Long schoolId = UserUtil.getSchoolId();
        QueryWrapper<StudentBaseInfo> existedQuery = new QueryWrapper<>();
        existedQuery.lambda().eq(StudentBaseInfo::getId, studentId).eq(StudentBaseInfo::getSchoolId, schoolId);
        StudentBaseInfo existedBaseInfo = baseMapper.selectOne(existedQuery);
        if (existedBaseInfo == null) {
            throw exception("学生信息不存在");
        }

        Long userId = existedBaseInfo.getUserId();
        SysUser sysUser = SysUserConvert.INSTANCE.convertVO(student);
        StudentBaseInfo baseInfo = SysUserConvert.INSTANCE.convertStudentVO(student);

        if (Objects.nonNull(student.getStudentStatus())) {
            StudentStatus studentStatus = StudentStatus.getStudentStatus(student.getStudentStatus());
            Assert.isFalse(StudentStatus.NONE.equals(studentStatus), "无效的学籍状态");
        }

        //根据身份证号验证数据是否已存在
        String identityCardNumber = StringUtils.upperCase(student.getIdentityCardNumber());
        if (StringUtils.isNotBlank(identityCardNumber)) {
            sysUser.setUsername(identityCardNumber);
            sysUser.setSex(UserUtil.getSex(identityCardNumber));
            baseInfo.setUsername(identityCardNumber);
            baseInfo.setSex(UserUtil.getSex(identityCardNumber));
            baseInfo.setBirthday(UserUtil.getBirthday(identityCardNumber));
        }

        //更新sys_user
        sysUser.setId(userId);
        SysUserDTO dto = userService.findByUserId(sysUser.getId(), false);

        if (StringUtils.isNotBlank(identityCardNumber) && dto.getFirstLogin() && !identityCardNumber.equals(existedBaseInfo.getIdentityCardNumber())) {
            sysUser.setPassword(EncryptorUtil.encrypt(identityCardNumber.substring(identityCardNumber.length() - 6)));
        }
        userService.updateUserPublic(sysUser);

        //更新student_base_info
        //根据年级设置入学年份
        String grade = student.getGrade();
        if (null != grade) {
            baseInfo.setAdmissionYear(UserUtil.getAdmissionYear(grade));
        }

        String parentMobile = student.getParentMobile();

        //家长手机号不为null
        if (Boolean.TRUE.equals(student.getIsUnbindParent())) {
            //解绑
            baseInfo.setParentId(null);
            baseInfo.setRelationship(null);

            //更新学生用户表的短id
            LambdaUpdateWrapper<SysUser> updateUserWrapper = new LambdaUpdateWrapper<>();
            updateUserWrapper.eq(SysUser::getId, userId).set(SysUser::getShortId, null);
            userService.update(null, updateUserWrapper);
        } else {
            if (StringUtils.isNotBlank(parentMobile)) {
                Parent parent = parentMapper.findByMobile(parentMobile, schoolId, this.key);
                Assert.notNull(parent, UserErrorCodeConstants.PARENT_NOT_EXISTS.getMessage());

                //换绑
                if (!parent.getId().equals(existedBaseInfo.getParentId())) {
                    baseInfo.setParentId(parent.getId());

                    //更新学生用户表的短id
                    SysUser user = new SysUser();
                    user.setId(userId);
                    user.setShortId(generateShortId(parent.getId(), student.getParentMobile()));
                    user.setUpdateBy(userId);
                    user.setUpdateTime(new Date());
                    userService.getBaseMapper().updateShortId(user, key);
                } else {
                    //将老数据的家长信息赋值给新数据
                    if (Objects.isNull(baseInfo.getParentId())) {
                        baseInfo.setParentId(existedBaseInfo.getParentId());
                    }
                    if (Objects.isNull(baseInfo.getRelationship())) {
                        baseInfo.setRelationship(existedBaseInfo.getRelationship());
                    }
                }
            } else {
                //将老数据的家长信息赋值给新数据
                if (Objects.isNull(baseInfo.getParentId())) {
                    baseInfo.setParentId(existedBaseInfo.getParentId());
                }
                if (Objects.isNull(baseInfo.getRelationship())) {
                    baseInfo.setRelationship(existedBaseInfo.getRelationship());
                }
                if (Objects.isNull(existedBaseInfo.getParentId())) {
                    baseInfo.setRelationship(null);
                }
            }
        }

        baseMapper.updateBatch(Collections.singletonList(baseInfo), key);

        //更新student_additional_info
        QueryWrapper<StudentAdditionalInfo> additionalInfoQueryWrapper = new QueryWrapper<>();
        additionalInfoQueryWrapper.lambda().eq(StudentAdditionalInfo::getStudentId, studentId);
        StudentAdditionalInfo existedAdditionalInfo = additionalInfoMapper.selectOne(additionalInfoQueryWrapper);

        StudentAdditionalInfo additionalInfo = SysUserConvert.INSTANCE.convertAdditionalVO(student);
        additionalInfo.setStudentId(studentId);
        additionalInfo.setId(existedAdditionalInfo.getId());
        additionalInfoMapper.updateBatch(Collections.singletonList(additionalInfo), key);

        //更新student_family_condition
        QueryWrapper<StudentFamilyCondition> deleteQuery = new QueryWrapper<>();
        deleteQuery.lambda().eq(StudentFamilyCondition::getStudentId, studentId);
        familyConditionMapper.delete(deleteQuery);

        List<Integer> conditions = student.getFamilyConditions();
        if (CollectionUtils.isNotEmpty(conditions)) {
            List<StudentFamilyCondition> familyConditions = new ArrayList<>();
            for (Integer condition : conditions) {
                StudentFamilyCondition studentFamilyCondition = new StudentFamilyCondition();
                studentFamilyCondition.setStudentId(studentId);
                studentFamilyCondition.setFamilyCondition(condition);
                familyConditions.add(studentFamilyCondition);
            }
            familyConditionMapper.saveBatch(familyConditions);
        }

        //缓存用户数据
        userService.cacheUser(userId);
    }

    /**
     * 生成学生短id
     *
     * @param parentId     家长id
     * @param parentMobile 家长手机号
     * @return 短id
     */
    private String generateShortId(Long parentId, String parentMobile) {
        int parentBoundStudentCount = baseMapper.selectCount(new LambdaQueryWrapper<StudentBaseInfo>().eq(StudentBaseInfo::getParentId, parentId));
        if (parentBoundStudentCount >= ShortId.getShortIdSuffix().length) {
            throw exception("一个家长最多绑定10个学生");
        }
        return parentMobile + ShortId.getShortIdSuffix()[parentBoundStudentCount];
    }

    @Override
    @Transactional
    public List<BatchOperationTipDTO> delete(Set<Long> ids) {
        List<BatchOperationTipDTO> resultMap = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(ids)) {
            Long schoolId = UserUtil.getSchoolId();
            Set<Long> delUser = new HashSet<>();

            for (Long id : ids) {
                StudentBaseInfo studentBaseInfo = studentExists(id, schoolId);
                if (Objects.isNull(studentBaseInfo)) {
                    resultMap.add(new BatchOperationTipDTO(id, "无效id"));
                    continue;
                }

                // 删除学生信息
                baseMapper.deleteById(id);
                // 删除学生补充信息
                additionalInfoMapper.delete(new LambdaQueryWrapper<StudentAdditionalInfo>().eq(StudentAdditionalInfo::getStudentId, id));
                // 删除学生家庭情况
                familyConditionMapper.delete(new LambdaQueryWrapper<StudentFamilyCondition>().eq(StudentFamilyCondition::getStudentId, id));
                // 清空该学生短id
                LambdaUpdateWrapper<SysUser> updateUserWrapper = new LambdaUpdateWrapper<>();
                updateUserWrapper.eq(SysUser::getId, studentBaseInfo.getUserId()).set(SysUser::getShortId, null);
                userService.update(null, updateUserWrapper);
                delUser.add(id);
            }
            // 删除用户表数据
            userService.delUser(delUser);
        }

        return resultMap;
    }

    /**
     * 学生列表
     *
     * @param params 查询条件
     * @return 分页数据
     */
    @Override
    public PageResult<StudentRespVO> list(Map<String, Object> params) {
        Page<StudentBaseInfo> page = new Page<>(MapUtil.getInt(params, Constant.PAGE_NUM, 1), MapUtil.getInt(params, Constant.PAGE_SIZE, 10));
        UserUtil.setSearchParams(params);

        params.put("schoolId", UserUtil.getSchoolId());

        //当前登录用户
        SysUserDTO sysUserDto = UserUtil.getCurrentUser();
        //当前登录用户角色编码
        String roleCode = sysUserDto.getPresetRoleCode();

        //当前用户角色为班主任
        if (RoleEnum.school_head_teacher.getValue().equals(roleCode)) {
            //查询班主任管理的班级信息
            List<ClazzDTO> clazzList = clazzService.getListByHeaderTeacher();
            if (CollectionUtils.isNotEmpty(clazzList)) {
                params.put("clazzIds", clazzList.stream().map(ClazzDTO::getId).collect(Collectors.toList()));
            }
        }

        page.setSearchCount(false);
        List<StudentRespVO> studentList = baseMapper.list(page, params, key);
        long total = baseMapper.listCount(params, key);
        page.setTotal(total);
        for (StudentRespVO studentRespVO : studentList) {
            if (StringUtils.isNotBlank(studentRespVO.getUsername())) {
                studentRespVO.setUsername(DesensitizedUtil.desensitized(studentRespVO.getUsername(), DesensitizedUtil.DesensitizedType.ID_CARD));
            }

            // 家长手机号脱敏
            if (StringUtils.isNotBlank(studentRespVO.getParentMobile())) {
                studentRespVO.setParentMobile(DesensitizedUtil.desensitized(studentRespVO.getParentMobile(), DesensitizedUtil.DesensitizedType.MOBILE_PHONE));
            }
        }

        return PageResult.<StudentRespVO>builder().data(studentList).count(page.getTotal()).build();
    }

    @Override
    public StudentInfoRespVO info(Long userId) {
        StudentInfoRespVO student = baseMapper.getById(userId, UserUtil.getSchoolId(), this.key);
        Assert.notNull(student, GlobalErrorCodeConstants.DATE_NOT_EXIST.getMessage());
        fillAdditionalInfo(student, student.getId());
        return student;
    }

    @Override
    public StudentInfoRespVO infoOnlyById(Long userId) {
        StudentInfoRespVO student = baseMapper.getById(userId, null, this.key);
        Assert.notNull(student, GlobalErrorCodeConstants.DATE_NOT_EXIST.getMessage());
        fillAdditionalInfo(student, student.getId());
        return student;
    }

    private void fillAdditionalInfo(StudentInfoRespVO student, Long studentId) {
        // 学生身份证号脱敏
        if (StringUtils.isNotBlank(student.getIdentityCardNumber())) {
            student.setIdentityCardNumber(DesensitizedUtil.desensitized(student.getIdentityCardNumber(), DesensitizedUtil.DesensitizedType.ID_CARD));
        }

        // 学生手机号脱敏
        if (StringUtils.isNotBlank(student.getMobile())) {
            student.setMobile(DesensitizedUtil.desensitized(student.getMobile(), DesensitizedUtil.DesensitizedType.MOBILE_PHONE));
        }

        //查询家长信息
        Parent parent = parentService.getById(student.getParentId());
        if (parent != null) {
            SysUserDTO parentDTO = userService.findByUserId(parent.getUserId(), false);
            if (parentDTO != null) {
                student.setParentName(parentDTO.getName());
                // 家长手机号脱敏
                if (StringUtils.isNotBlank(parentDTO.getMobile())) {
                    student.setParentMobile(DesensitizedUtil.desensitized(parentDTO.getMobile(), DesensitizedUtil.DesensitizedType.MOBILE_PHONE));
                }
            }
        }

        //查询家庭信息
        QueryWrapper<StudentFamilyCondition> familyConditionWrapper = new QueryWrapper<>();
        familyConditionWrapper.lambda().eq(StudentFamilyCondition::getStudentId, studentId);
        List<StudentFamilyCondition> familyConditions = familyConditionMapper.selectList(familyConditionWrapper);

        for (StudentFamilyCondition familyCondition : familyConditions) {
            student.getFamilyConditions().add(familyCondition.getFamilyCondition());
        }
    }

    @Override
    public StudentBaseInfoRespVO baseInfo(Long userId) {
        StudentBaseInfo studentBaseInfo = baseMapper.findByUserId(userId, UserUtil.getSchoolId(), this.key);
        StudentBaseInfoRespVO student = SysUserConvert.INSTANCE.convertStudentBaseVO(studentBaseInfo);
        Clazz clazz = clazzMapper.selectById(student.getClazzId());
        Assert.notNull(clazz, "获取学生班级信息失败");
        student.setClazzName(clazz.getClazzName());
        // 账号名(手机号)脱敏
        UserUtil.desensitizedMobile(student);
        // 学生身份证号脱敏
        if (StringUtils.isNotBlank(student.getIdentityCardNumber())) {
            student.setIdentityCardNumber(DesensitizedUtil.desensitized(student.getIdentityCardNumber(), DesensitizedUtil.DesensitizedType.ID_CARD));
        }
        return student;
    }

    @Override
    public void updateCurrentStudent(StudentBaseReqVO studentReqVO) throws Exception {
        Long userId = UserUtil.getUserId();
        StudentBaseInfo studentBaseInfoOld = baseMapper.findByUserId(userId, UserUtil.getSchoolId(), this.key);
        Assert.notNull(studentBaseInfoOld, GlobalErrorCodeConstants.DATE_NOT_EXIST.getMessage());
        // 更新用户信息
        SysUser user = new SysUser();
        user.setId(studentBaseInfoOld.getUserId());
        user.setNickname(studentReqVO.getNickname());
        user.setEmail(studentReqVO.getEmail());
        user.setHeadImgUrl(studentReqVO.getHeadImgUrl());
        userService.updateUserPublic(user);
        if (studentReqVO.getEmail() != null) {
            StudentBaseInfo baseInfo = new StudentBaseInfo();
            baseInfo.setId(studentBaseInfoOld.getId());
            baseInfo.setEmail(studentReqVO.getEmail());
            baseMapper.updateCurrentStudentBaseInfo(baseInfo);
        }
        // 缓存用户数据
        userService.cacheUser(user.getId());
    }

    @Override
    public void deleteBySchoolId(Long schoolId) {
        Set<Long> studentIds = baseMapper.getStudentIdBySchool(schoolId);
        if (CollectionUtils.isNotEmpty(studentIds)) {
            // 删除学生信息
            baseMapper.deleteBatchIds(studentIds);
            // 删除学生补充信息
            additionalInfoMapper.delete(new LambdaQueryWrapper<StudentAdditionalInfo>().in(StudentAdditionalInfo::getStudentId, studentIds));
            // 删除学生家庭情况
            familyConditionMapper.delete(new LambdaQueryWrapper<StudentFamilyCondition>().in(StudentFamilyCondition::getStudentId, studentIds));
        }
    }

    @Override
    public void deleteByClazzId(Long clazzId) {
        Set<Long> studentIds = baseMapper.getStudentIdByClazz(clazzId);
        if (CollectionUtils.isNotEmpty(studentIds)) {
            // 删除学生信息
            baseMapper.deleteBatchIds(studentIds);
            // 删除学生补充信息
            additionalInfoMapper.delete(new LambdaQueryWrapper<StudentAdditionalInfo>().in(StudentAdditionalInfo::getStudentId, studentIds));
            // 删除学生家庭情况
            familyConditionMapper.delete(new LambdaQueryWrapper<StudentFamilyCondition>().in(StudentFamilyCondition::getStudentId, studentIds));
        }
    }

    @Override
    public Set<Long> getUserIdByClazz(Long clazzId) {
        return baseMapper.getUserIdByClazz(clazzId);
    }

    @Override
    public PageResult<StudentBaseRespVO> listBaseInfo(Map<String, Object> params) {
        Page<StudentBaseInfo> page = new Page<>(MapUtil.getInt(params, Constant.PAGE_NUM, 1), MapUtil.getInt(params, Constant.PAGE_SIZE, 10));
        params.put("schoolId", UserUtil.getSchoolId());

        List<StudentBaseRespVO> studentList = baseMapper.listBaseInfo(page, params, key);
        for (StudentBaseRespVO baseRespVO : studentList) {
            if (StringUtils.isNotBlank(baseRespVO.getIdentityCardNumber())) {
                baseRespVO.setIdentityCardNumber(DesensitizedUtil.desensitized(baseRespVO.getIdentityCardNumber(), DesensitizedUtil.DesensitizedType.ID_CARD));
            }
            baseRespVO.setHeadImgUrl(userService.getHeadImgUrlById(baseRespVO.getUserId()));
        }
        return PageResult.<StudentBaseRespVO>builder().data(studentList).count(page.getTotal()).build();
    }

    /**
     * 根据id和学校id查看数据是否存在
     *
     * @param id       学生id
     * @param schoolId 学校id
     * @return 学生信息
     */
    public StudentBaseInfo studentExists(Long id, Long schoolId) {
        QueryWrapper<StudentBaseInfo> existedQuery = new QueryWrapper<>();
        existedQuery.lambda().eq(StudentBaseInfo::getId, id).eq(StudentBaseInfo::getSchoolId, schoolId);
        return baseMapper.selectOne(existedQuery);
    }

    @Override
    public List<StudentBaseInfoDTO> getStudentListByUserIds(Set<Long> userIds) {
        return baseMapper.getStudentListByUserIds(userIds, key);
    }

    @Override
    public StudentBaseInfoDTO getStudentListByUserId(Long userId) {
        return baseMapper.getStudentListByUserId(userId, key);
    }

    @Override
    public List<Long> getUserIdsByQuery(Map<String, Object> params) {
        return baseMapper.getUserIdsByQuery(params, key);
    }

    @Override
    public List<Long> getUserIdsByName(String name) {
        return baseMapper.getUserIdsByName(name, key);
    }

    @Override
    public List<Map<String, Object>> getSelectList(Long schoolId, String userName, Long clazzId, String idCard, String studentNumber) {
        List<Map<String, Object>> studentList = baseMapper.getSelectList(schoolId, userName, clazzId, idCard, studentNumber, key);
        for (Map<String, Object> student : studentList) {
            student.put("identity_card_number", DesensitizedUtil.desensitized(student.get("identity_card_number").toString(), DesensitizedUtil.DesensitizedType.ID_CARD));
        }
        return studentList;
    }

    @Override
    public List<Long> getStudentUserIdByParams(Map<String, Object> params) {
        return baseMapper.getStudentUserIdByParams(params, key);
    }

    @Override
    public List<UserClazzVO> getStudentUserIdClazzIdBySchoolAndClass(Long schoolId, List<Long> clazzIds) {
        return baseMapper.getStudentUserIdClazzIdBySchoolAndClass(schoolId, clazzIds);
    }

    @Override
    public List<StudentDTO> findStudentListByIds(Set<Long> userIds) {
        return baseMapper.findStudentListByIds(userIds, key);
    }

    @Override
    public StudentDTO findStudentGradeAndIDNumber(Long userId) {
        return baseMapper.findStudentGradeAndIDNumber(userId, key);
    }

    @Override
    public StudentDTO findStudentInfo(Long userId) {
        StudentDTO studentInfo = baseMapper.findStudentInfo(userId, key);
        if (Objects.nonNull(studentInfo) && studentInfo.isSchool()) {
            Long schoolId = studentInfo.getSchoolId();
            Map<Long, SchoolDTO> schoolMap = DataCacheUtil.getSchoolMap();
            if (schoolMap.containsKey(schoolId)) {
                studentInfo.setSchoolName(schoolMap.get(schoolId).getName());
            }
        }
        return studentInfo;
    }

    @Override
    public List<Long> findStudentUserIdByParentUserId(Long userId) {
        return baseMapper.findStudentUserIdByParentUserId(userId);
    }
}
