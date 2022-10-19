package com.cnasoft.health.userservice.service.impl;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.ClazzDTO;
import com.cnasoft.health.common.dto.SysUserDTO;
import com.cnasoft.health.common.enums.RoleEnum;
import com.cnasoft.health.common.model.CommonModel;
import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.convert.ClazzConvert;
import com.cnasoft.health.userservice.convert.SysUserConvert;
import com.cnasoft.health.userservice.feign.dto.ClazzReqDTO;
import com.cnasoft.health.userservice.mapper.ClazzMapper;
import com.cnasoft.health.userservice.mapper.SchoolStaffClazzMapper;
import com.cnasoft.health.userservice.model.Clazz;
import com.cnasoft.health.userservice.model.SchoolStaffClazz;
import com.cnasoft.health.userservice.model.SysUser;
import com.cnasoft.health.userservice.service.IClazzService;
import com.cnasoft.health.userservice.service.IStudentBaseInfoService;
import com.cnasoft.health.userservice.service.ISysUserService;
import com.cnasoft.health.userservice.util.UserUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.cnasoft.health.common.exception.util.ServiceExceptionUtil.exception;

/**
 * @author lqz
 * 针对表【class】的数据库操作Service实现
 * 2022-04-14
 */
@Service
public class ClazzServiceImpl extends SuperServiceImpl<ClazzMapper, Clazz> implements IClazzService {

    @Value("${user.password.key}")
    private String key;

    @Resource
    private SchoolStaffClazzMapper schoolStaffClazzMapper;

    @Resource
    private SchoolStaffClazzServiceImpl schoolStaffClazzService;

    @Resource
    private IStudentBaseInfoService studentBaseInfoService;

    @Resource
    private ISysUserService sysUserService;

    /**
     * 分页查询班级
     *
     * @param params 查询条件
     * @return 分页数据
     */
    @Override
    public PageResult<ClazzDTO> findList(Map<String, Object> params) {
        Page<Clazz> page = new Page<>(MapUtil.getInt(params, Constant.PAGE_NUM, 1), MapUtil.getInt(params, Constant.PAGE_SIZE, 10));
        params.put("schoolId", UserUtil.getSchoolId());
        List<Clazz> classList = baseMapper.findList(page, params, this.key);
        List<ClazzDTO> clazzDTOList = ClazzConvert.INSTANCE.convertList(classList);
        long total = page.getTotal();

        //查询班级的班主任信息
        if (total > 0) {
            List<Long> clazzIds = clazzDTOList.stream().map(ClazzDTO::getId).collect(Collectors.toList());
            List<SysUser> sysUsers = schoolStaffClazzMapper.headteacherList(clazzIds, key);

            List<CommonModel> commonModels = SysUserConvert.INSTANCE.convertCommonModel(sysUsers);

            for (ClazzDTO clazz : clazzDTOList) {
                clazz.setHeaderTeachers(new ArrayList<>());
                for (CommonModel model : commonModels) {
                    if (clazz.getId().equals(model.getClazzId())) {
                        clazz.getHeaderTeachers().add(model);
                    }
                }
            }
        }
        return PageResult.<ClazzDTO>builder().data(clazzDTOList).count(total).build();
    }

    @Override
    public List<ClazzDTO> listAll(Long schoolId) {
        //当前登录人
        SysUserDTO currentUser = UserUtil.getCurrentUser();
        //当前登录人角色编码
        String roleCode = currentUser.getPresetRoleCode();
        if (RoleEnum.school_head_teacher.getValue().equals(roleCode)) {
            return baseMapper.getListByHeaderTeacher(UserUtil.getSchoolId(), currentUser.getId());
        }

        LambdaQueryWrapper<Clazz> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Clazz::getSchoolId, schoolId).orderByAsc(Clazz::getGrade);
        return ClazzConvert.INSTANCE.convertList(baseMapper.selectList(queryWrapper));
    }

    @Override
    public List<ClazzDTO> listClazzByGrade(String grade) {
        QueryWrapper<Clazz> classQuery = new QueryWrapper<>();
        classQuery.lambda().eq(Clazz::getGrade, grade);
        List<Clazz> classList = this.list(classQuery);

        return ClazzConvert.INSTANCE.convertList(classList);
    }

    @Override
    public List<ClazzDTO> listClazzByClazzName(String clazzName) {
        QueryWrapper<Clazz> classQuery = new QueryWrapper<>();
        classQuery.lambda().eq(Clazz::getClazzName, clazzName);
        List<Clazz> classList = baseMapper.selectList(classQuery);

        return ClazzConvert.INSTANCE.convertList(classList);
    }

    @Override
    public ClazzDTO getClazzDetail(String grade, String clazzName) {
        QueryWrapper<Clazz> classQuery = new QueryWrapper<>();
        classQuery.lambda().eq(Clazz::getGrade, grade).eq(Clazz::getClazzName, clazzName);
        Clazz clazzGet = baseMapper.selectOne(classQuery);

        return ClazzConvert.INSTANCE.convertDTO(clazzGet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveClazz(ClazzReqDTO clazzDTO) {
        Long schoolId = UserUtil.getSchoolId();
        String grade = clazzDTO.getGrade();
        String clazzName = clazzDTO.getClazzName();

        //验证数据是否已存在
        LambdaQueryWrapper<Clazz> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Clazz::getSchoolId, schoolId);
        queryWrapper.eq(Clazz::getGrade, grade);
        queryWrapper.eq(Clazz::getClazzName, clazzName);

        Clazz existClazz = baseMapper.selectOne(queryWrapper);
        if (Objects.nonNull(existClazz)) {
            throw exception("该班级在这个年级已经存在");
        }

        Clazz clazzData = ClazzConvert.INSTANCE.convertDb(clazzDTO);
        clazzData.setSchoolId(schoolId);
        clazzData.setAdmissionDate(UserUtil.getAdmissionYear(grade));
        baseMapper.insert(clazzData);

        saveSchoolStaffClazz(clazzData.getId(), clazzDTO.getHeaderTeacherIds(), true);
    }

    private void saveSchoolStaffClazz(Long clazzId, List<Long> headerTeachers, boolean add) {
        if (!add) {
            LambdaQueryWrapper<SchoolStaffClazz> queryWrapper = new LambdaQueryWrapper<>();
            schoolStaffClazzService.remove(queryWrapper.eq(SchoolStaffClazz::getClazzId, clazzId));
        }

        if (CollectionUtils.isNotEmpty(headerTeachers)) {
            List<SchoolStaffClazz> staffClazzList = new ArrayList<>();
            for (Long schoolStaffId : headerTeachers) {
                SchoolStaffClazz staffClazz = new SchoolStaffClazz();
                staffClazz.setClazzId(clazzId);
                staffClazz.setSchoolStaffId(schoolStaffId);
                staffClazzList.add(staffClazz);
            }
            schoolStaffClazzService.saveBatch(staffClazzList);
        }
    }

    @Override
    public void updateClazz(ClazzReqDTO clazzDTO) {
        Long clazzId = clazzDTO.getId();
        Long schoolId = UserUtil.getSchoolId();

        LambdaQueryWrapper<Clazz> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Clazz::getSchoolId, schoolId);
        queryWrapper.eq(Clazz::getId, clazzId);

        Clazz clazzExist = baseMapper.selectOne(queryWrapper);
        if (clazzExist == null) {
            throw exception("需要修改的班级不存在");
        }

        LambdaQueryWrapper<Clazz> clazzUpdateWrapper = new LambdaQueryWrapper<>();
        clazzUpdateWrapper.eq(Clazz::getSchoolId, schoolId);
        clazzUpdateWrapper.eq(Clazz::getGrade, clazzDTO.getGrade());
        clazzUpdateWrapper.eq(Clazz::getClazzName, clazzDTO.getClazzName());
        clazzUpdateWrapper.ne(Clazz::getId, clazzId);

        Clazz clazzUpdate = baseMapper.selectOne(clazzUpdateWrapper);
        if (clazzUpdate != null) {
            throw exception("该班级名称重复");
        }

        Clazz clazzData = ClazzConvert.INSTANCE.convertDb(clazzDTO);
        baseMapper.updateById(clazzData);

        saveSchoolStaffClazz(clazzData.getId(), clazzDTO.getHeaderTeacherIds(), false);
    }

    @Override
    public List<BatchOperationTipDTO> deleteClazz(Set<Long> ids) {
        List<BatchOperationTipDTO> resultMap = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(ids)) {
            Set<Long> userIds = new HashSet<>();
            for (Long id : ids) {
                Clazz clazz = baseMapper.selectById(id);
                if (ObjectUtils.isEmpty(clazz)) {
                    resultMap.add(new BatchOperationTipDTO(id, "无效id"));
                    continue;
                }
                userIds.addAll(studentBaseInfoService.getUserIdByClazz(id));
                baseMapper.deleteById(id);
                //删除班主任
                LambdaQueryWrapper<SchoolStaffClazz> staffClazzWrapper = new LambdaQueryWrapper<>();
                staffClazzWrapper.eq(SchoolStaffClazz::getClazzId, id);
                schoolStaffClazzMapper.delete(staffClazzWrapper);
                //删除学生
                studentBaseInfoService.deleteByClazzId(id);
            }
            //删除用户信息
            sysUserService.delUser(userIds);
        }
        return resultMap;
    }

    @Override
    public List<ClazzDTO> getListByHeaderTeacher() {
        //当前登录人
        SysUserDTO currentUser = UserUtil.getCurrentUser();
        //当前登录人角色编码
        String roleCode = currentUser.getPresetRoleCode();
        if (RoleEnum.school_head_teacher.getValue().equals(roleCode)) {
            return baseMapper.getListByHeaderTeacher(UserUtil.getSchoolId(), currentUser.getId());
        }

        return Collections.emptyList();
    }

    @Override
    public List<ClazzDTO> getListByIds(Set<Long> ids) {
        List<Clazz> clazzList = baseMapper.selectList(new LambdaUpdateWrapper<Clazz>().in(Clazz::getId, ids));
        return ClazzConvert.INSTANCE.convertList(clazzList);
    }

    @Override
    public void deleteBySchoolId(Long schoolId) {
        Set<Long> clazzIds = baseMapper.getClazzIdBySchool(schoolId);
        LambdaQueryWrapper<Clazz> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Clazz::getSchoolId, schoolId);
        baseMapper.delete(wrapper);
        if (CollectionUtils.isNotEmpty(clazzIds)) {
            LambdaQueryWrapper<SchoolStaffClazz> staffClazzWrapper = new LambdaQueryWrapper<>();
            staffClazzWrapper.in(SchoolStaffClazz::getClazzId, clazzIds);
            schoolStaffClazzMapper.delete(staffClazzWrapper);
        }
    }

    @Override
    public Boolean checkHeaderTeacherWithStudent(Long headerTeacherUserId, Long studentUserId) {
        return baseMapper.checkHeaderTeacherWithStudent(headerTeacherUserId, studentUserId);
    }

    @Override
    public List<Long> getClazzBySchoolIdAndGrade(Long schoolId, String grade) {
        return baseMapper.getClazzBySchoolIdAndGrade(schoolId, grade);
    }
}
