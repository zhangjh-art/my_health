package com.cnasoft.health.userservice.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.CommonDTO;
import com.cnasoft.health.common.dto.SchoolDTO;
import com.cnasoft.health.common.dto.SchoolStaffDTO;
import com.cnasoft.health.common.dto.SchoolTeacherStaffDTO;
import com.cnasoft.health.common.encryptor.DesensitizedUtil;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.enums.RoleEnum;
import com.cnasoft.health.common.enums.SchoolStaffType;
import com.cnasoft.health.common.exception.constant.GlobalErrorCodeConstants;
import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.common.vo.UserClazzVO;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.convert.SchoolStaffConvert;
import com.cnasoft.health.userservice.convert.SysUserConvert;
import com.cnasoft.health.userservice.feign.dto.SchoolStaffReqVO;
import com.cnasoft.health.userservice.feign.dto.SchoolStaffRespVO;
import com.cnasoft.health.userservice.feign.dto.SchoolTeacherRespVO;
import com.cnasoft.health.userservice.feign.dto.StaffMentalFileVO;
import com.cnasoft.health.userservice.feign.dto.UserRespVO;
import com.cnasoft.health.userservice.mapper.SchoolStaffMapper;
import com.cnasoft.health.userservice.model.SchoolStaff;
import com.cnasoft.health.userservice.model.SysUser;
import com.cnasoft.health.userservice.service.ISchoolStaffService;
import com.cnasoft.health.userservice.service.ISchoolTeacherService;
import com.cnasoft.health.userservice.service.ISmsRecordService;
import com.cnasoft.health.userservice.service.ISysUserService;
import com.cnasoft.health.userservice.util.DataCacheUtil;
import com.cnasoft.health.userservice.util.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.cnasoft.health.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 校教职工
 *
 * @author ganghe
 */
@Slf4j
@Service
public class SchoolStaffServiceImpl extends SuperServiceImpl<SchoolStaffMapper, SchoolStaff> implements ISchoolStaffService {

    @Value("${user.password.key}")
    private String key;

    @Resource
    private ISysUserService userService;

    @Resource
    private ISmsRecordService smsRecordService;

    @Resource
    private ISchoolTeacherService schoolTeacherService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SchoolStaff add(SchoolStaffReqVO staffReqVO) throws Exception {
        Long schoolId = UserUtil.getSchoolId();

        SchoolStaff schoolStaff = SchoolStaffConvert.INSTANCE.convert(staffReqVO);
        schoolStaff.setSchoolId(schoolId);

        SysUser user = SysUserConvert.INSTANCE.convertWithSchoolStaff(staffReqVO);
        user.setSchoolId(schoolId);
        user.setApproveStatus(ApproveStatus.APPROVED.getCode());

        RoleEnum roleEnum;
        Integer staffType = schoolStaff.getType();
        if (SchoolStaffType.HEAD_MASTER.getCode().equals(staffType)) {
            roleEnum = RoleEnum.school_head_teacher;
        } else if (SchoolStaffType.LEADER.getCode().equals(staffType)) {
            roleEnum = RoleEnum.school_leader;
        } else {
            roleEnum = RoleEnum.school_staff;
        }

        boolean result = userService.saveUserPublic(user, roleEnum);
        if (result) {
            Long userId = user.getId();
            //保存校教职工
            schoolStaff.setUserId(userId);
            baseMapper.insert(schoolStaff);
        }
        return schoolStaff;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(SchoolStaffReqVO staffReqVO) throws Exception {
        Long schoolId = UserUtil.getSchoolId();
        SchoolStaff oldStaff = staffExists(staffReqVO.getId(), schoolId);
        Assert.notNull(oldStaff, GlobalErrorCodeConstants.DATE_NOT_EXIST.getMessage());

        // 更新用户信息
        SysUser user = SysUserConvert.INSTANCE.convertWithSchoolStaff(staffReqVO);
        user.setId(oldStaff.getUserId());

        Integer staffType = staffReqVO.getType();
        if (Objects.nonNull(staffType) && !oldStaff.getType().equals(staffType)) {
            RoleEnum roleEnum;
            if (SchoolStaffType.HEAD_MASTER.getCode().equals(staffType)) {
                roleEnum = RoleEnum.school_head_teacher;
            } else if (SchoolStaffType.LEADER.getCode().equals(staffType)) {
                roleEnum = RoleEnum.school_leader;
            } else {
                roleEnum = RoleEnum.school_staff;
            }
            user.setRoleCode(roleEnum.getValue());
        }

        userService.updateUserPublic(user);

        SchoolStaff schoolTeacher = SchoolStaffConvert.INSTANCE.convert(staffReqVO);
        baseMapper.updateById(schoolTeacher);

        // 缓存新账号数据
        userService.cacheUser(user.getId());
    }

    /**
     * param：①id/手机号/账号状态（用户表） ③工号（老师表）
     *
     * @param params 查询条件
     * @return 分页数据
     */
    @Override
    public PageResult<SchoolStaffRespVO> findList(Map<String, Object> params) {
        Page<SchoolStaff> page = new Page<>(MapUtil.getInt(params, Constant.PAGE_NUM, 1), MapUtil.getInt(params, Constant.PAGE_SIZE, 10));
        UserUtil.setSearchParams(params);
        params.put("schoolId", UserUtil.getSchoolId());

        List<SchoolStaff> staffs = baseMapper.findList(page, params, key);
        List<SchoolStaffRespVO> staffList = SchoolStaffConvert.INSTANCE.convert2List(staffs);
        // 账号名(手机号)脱敏
        staffList.forEach(UserUtil::desensitizedMobile);

        return PageResult.<SchoolStaffRespVO>builder().data(staffList).count(page.getTotal()).build();
    }

    /**
     * 批量删除校教职工
     *
     * @param ids id列表
     * @return 行数
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<BatchOperationTipDTO> delete(Set<Long> ids) {
        List<BatchOperationTipDTO> resultMap = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(ids)) {
            Long schoolId = UserUtil.getSchoolId();

            for (Long id : ids) {
                SchoolStaff schoolStaff = staffExists(id, schoolId);
                if (ObjectUtils.isEmpty(schoolStaff)) {
                    resultMap.add(new BatchOperationTipDTO(id, "无效id"));
                    continue;
                }

                LambdaQueryWrapper<SchoolStaff> deleteWrapper = new LambdaQueryWrapper<>();
                deleteWrapper.eq(SchoolStaff::getSchoolId, schoolId);
                deleteWrapper.eq(SchoolStaff::getId, id);
                baseMapper.delete(deleteWrapper);

                userService.delUser(Collections.singleton(schoolStaff.getUserId()));
            }
        }
        return resultMap;
    }

    /**
     * 根据id和学校id查看数据是否存在
     *
     * @param id       校职员id
     * @param schoolId 学校id
     * @return 校职员
     */
    public SchoolStaff staffExists(Long id, Long schoolId) {
        return baseMapper.findByIdAndSchoolId(id, schoolId, key);
    }

    /**
     * 班主任列表
     *
     * @return 列表数据
     */
    @Override
    public List<CommonDTO> headerTeacherList() {
        Long schoolId = UserUtil.getSchoolId();

        return baseMapper.headerTeacherList(schoolId, 2, key);
    }

    @Override
    public SchoolStaffRespVO findByUserId(Long userId) {
        SchoolStaff schoolStaff = baseMapper.findByUserId(userId, key);
        Assert.notNull(schoolStaff, GlobalErrorCodeConstants.DATE_NOT_EXIST.getMessage());

        SchoolStaffRespVO staffRespVO = SchoolStaffConvert.INSTANCE.convert(schoolStaff);
        UserUtil.desensitizedMobile(staffRespVO);
        return staffRespVO;
    }

    @Override
    public void updateCurrentSchoolStaff(SchoolStaffReqVO staffReqVO) throws Exception {
        if (StringUtils.isEmpty(staffReqVO.getVerifyCode())) {
            //未修改手机号
            staffReqVO.setMobile(null);
        } else {
            //校验手机号
            smsRecordService.checkCaptcha(staffReqVO.getMobile(), staffReqVO.getVerifyCode());
        }
        Long userId = UserUtil.getUserId();
        SchoolStaff schoolStaffOld = baseMapper.findByUserId(userId, key);
        Assert.notNull(schoolStaffOld, GlobalErrorCodeConstants.DATE_NOT_EXIST.getMessage());

        // 更新用户信息
        SysUser user = new SysUser();
        user.setId(schoolStaffOld.getUserId());
        user.setName(staffReqVO.getName());
        user.setSex(staffReqVO.getSex());
        user.setEmail(staffReqVO.getEmail());
        user.setMobile(staffReqVO.getMobile());
        user.setHeadImgUrl(staffReqVO.getHeadImgUrl());
        user.setNickname(staffReqVO.getNickname());
        userService.updateUserPublic(user);

        SchoolStaff schoolStaff = new SchoolStaff();
        schoolStaff.setId(schoolStaffOld.getId());
        schoolStaff.setDepartment(staffReqVO.getDepartment());
        schoolStaff.setPost(staffReqVO.getPost());
        baseMapper.updateById(schoolStaff);

        // 缓存用户数据
        userService.cacheUser(user.getId());
    }

    @Override
    public void deleteBySchoolId(Long schoolId) {
        LambdaQueryWrapper<SchoolStaff> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SchoolStaff::getSchoolId, schoolId);
        baseMapper.delete(wrapper);
    }

    @Override
    public List<Map<String, Object>> getSelectList(Long schoolId, String userName, String department) {
        return baseMapper.getSelectList(schoolId, userName, department, key);
    }

    @Override
    public PageResult<StaffMentalFileVO> listMentalFile(Map<String, Object> params) throws IllegalAccessException {
        Page<StaffMentalFileVO> page = new Page<>(MapUtil.getInt(params, Constant.PAGE_NUM, 1), MapUtil.getInt(params, Constant.PAGE_SIZE, 10));
        UserUtil.setSearchParams(params);
        params.put("schoolId", UserUtil.getSchoolId());

        SchoolTeacherRespVO teacher = schoolTeacherService.findByUserId(UserUtil.getUserId());
        if (teacher == null || !teacher.getIsAcceptTask()) {
            throw exception("非任务承接人无法查看心理档案");
        }

        List<StaffMentalFileVO> staffs = baseMapper.listMentalFile(page, params, key);
        // 账号名(手机号)脱敏
        staffs.forEach(e -> {
            if (StringUtils.isNotBlank(e.getMobile())) {
                e.setMobile(DesensitizedUtil.desensitized(e.getMobile(), DesensitizedUtil.DesensitizedType.MOBILE_PHONE));
            }
        });

        return PageResult.<StaffMentalFileVO>builder().data(staffs).count(page.getTotal()).build();
    }

    @Override
    public UserRespVO findUnionUser(Long userId) {
        SysUser sysUser = userService.selectSysUserById(userId);
        if (RoleEnum.school_psycho_teacher.getValue().equals(sysUser.getRoleCode())) {
            return schoolTeacherService.findByUserId(userId);
        } else {
            return findByUserId(userId);
        }
    }

    @Override
    public List<UserClazzVO> getSchoolStaffUserIdBySchoolAndDepartmentCode(Long schoolId, List<String> departmentCodes) {
        return baseMapper.getSchoolStaffUserIdBySchoolAndDepartmentCode(schoolId, departmentCodes);
    }

    @Override
    public SchoolStaffDTO findSchoolStaffInfo(Long userId) {
        SchoolStaffDTO schoolStaff = baseMapper.findSchoolStaffInfo(userId);
        if (Objects.nonNull(schoolStaff) && schoolStaff.isSchool()) {
            Map<Long, SchoolDTO> schoolMap = DataCacheUtil.getSchoolMap();
            Long schoolId = schoolStaff.getSchoolId();
            if (schoolMap.containsKey(schoolId)) {
                schoolStaff.setSchoolName(schoolMap.get(schoolId).getName());
            }
        }
        return schoolStaff;
    }

    @Override
    public List<SchoolTeacherStaffDTO> findSchoolTeacherStaffList(Set<Long> userIds) {
        List<SchoolTeacherStaffDTO> teacherStaffList = baseMapper.findSchoolTeacherStaffList(userIds, key);
        if (CollectionUtils.isNotEmpty(teacherStaffList)) {
            Map<Long, SchoolDTO> schoolMap = DataCacheUtil.getSchoolMap();
            for (SchoolTeacherStaffDTO teacherStaff : teacherStaffList) {
                if (teacherStaff.isSchool()) {
                    Long schoolId = teacherStaff.getSchoolId();
                    if (schoolMap.containsKey(schoolId)) {
                        teacherStaff.setSchoolName(schoolMap.get(schoolId).getName());
                    }
                }
            }
        }
        return teacherStaffList;
    }
}
