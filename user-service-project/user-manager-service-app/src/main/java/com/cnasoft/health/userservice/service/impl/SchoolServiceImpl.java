package com.cnasoft.health.userservice.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.CommonDTO;
import com.cnasoft.health.common.dto.SchoolDTO;
import com.cnasoft.health.common.dto.SysAreaDTO;
import com.cnasoft.health.common.dto.SysUserDTO;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.enums.RoleEnum;
import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.convert.SchoolConvert;
import com.cnasoft.health.userservice.feign.dto.SchoolReqVO;
import com.cnasoft.health.userservice.mapper.SchoolMapper;
import com.cnasoft.health.userservice.model.School;
import com.cnasoft.health.userservice.service.IClazzService;
import com.cnasoft.health.userservice.service.IParentService;
import com.cnasoft.health.userservice.service.ISchoolService;
import com.cnasoft.health.userservice.service.ISchoolStaffService;
import com.cnasoft.health.userservice.service.ISchoolTeacherService;
import com.cnasoft.health.userservice.service.IStudentBaseInfoService;
import com.cnasoft.health.userservice.service.ISysUserService;
import com.cnasoft.health.userservice.util.AreaUtil;
import com.cnasoft.health.userservice.util.DataCacheUtil;
import com.cnasoft.health.userservice.util.UserUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 针对表【school(学校表)】的数据库操作Service实现
 *
 * @author lqz
 * <p>
 * 2022-04-11
 */
@Service
public class SchoolServiceImpl extends SuperServiceImpl<SchoolMapper, School> implements ISchoolService {

    @Resource
    private AreaUtil areaUtil;

    @Resource
    SchoolMapper schoolMapper;

    @Resource
    ISchoolTeacherService schoolTeacherService;

    @Resource
    ISchoolStaffService schoolStaffService;

    @Resource
    ISysUserService sysUserService;

    @Resource
    IClazzService clazzService;

    @Resource
    IStudentBaseInfoService studentBaseInfoService;

    @Resource
    IParentService parentService;

    @Override
    public PageResult<SchoolDTO> listSchool(Map<String, Object> params) {
        Page<SchoolDTO> page = new Page<>(MapUtil.getInt(params, Constant.PAGE_NUM, 1), MapUtil.getInt(params, Constant.PAGE_SIZE, 10));
        UserUtil.setSearchParams(params);

        List<School> schools = baseMapper.findList(page, params);
        long total = page.getTotal();

        List<SchoolDTO> schoolDTOList = SchoolConvert.INSTANCE.convertList(schools);
        for (SchoolDTO schoolItem : schoolDTOList) {
            //设置区/县名
            Integer areaCode = schoolItem.getAreaCode();
            schoolItem.setDistinct(areaCode);
            //设置市名
            schoolItem.setCity(areaCode / 100 * 100);
            //设置省
            schoolItem.setProvince(areaCode / 10000 * 10000);
        }

        return PageResult.<SchoolDTO>builder().data(schoolDTOList).count(total).build();
    }

    @Override
    public List<CommonDTO> listSchool(Set<Long> schoolIds) {
        LambdaQueryWrapper<School> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(School::getId, schoolIds);
        List<School> schools = baseMapper.selectList(queryWrapper);

        return SchoolConvert.INSTANCE.convertCommon(schools);
    }

    @Override
    public List<SchoolDTO> listAllSchool(List<Integer> areaCodes) {
        List<SchoolDTO> schoolDTOS = new ArrayList<>();

        //获取当前登录用户信息
        SysUserDTO sysUser = UserUtil.getCurrentUser();
        LambdaQueryWrapper<School> schoolQuery = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<School> queryWrapper = schoolQuery.eq(School::getIsDeleted, 0);
        //角色判断
        String roleCode = sysUser.getPresetRoleCode();
        if (RoleEnum.region_admin.getValue().equals(roleCode) || RoleEnum.region_leader.getValue().equals(roleCode) || RoleEnum.region_psycho_teacher.getValue().equals(roleCode)
            || RoleEnum.region_staff.getValue().equals(roleCode)) {
            //区域级角色只能查看本区域学校
            areaCodes = new ArrayList<>();
            areaCodes.add(sysUser.getAreaCode());
            queryWrapper.in(School::getAreaCode, areaCodes);

        } else if (RoleEnum.student.getValue().equals(roleCode) || RoleEnum.school_staff.getValue().equals(roleCode) || RoleEnum.parents.getValue().equals(roleCode)
            || RoleEnum.school_head_teacher.getValue().equals(roleCode) || RoleEnum.school_admin.getValue().equals(roleCode) || RoleEnum.school_leader.getValue().equals(roleCode)
            || RoleEnum.school_psycho_teacher.getValue().equals(roleCode)) {
            return schoolDTOS;
        } else if (RoleEnum.admin.getValue().equals(roleCode) || RoleEnum.first_level_admin.getValue().equals(roleCode) || RoleEnum.second_level_admin.getValue()
            .equals(roleCode)) {
            if (areaCodes != null && areaCodes.size() > 0) {
                queryWrapper.in(School::getAreaCode, areaCodes);
            }
        }

        schoolQuery.eq(School::getApproveStatus, ApproveStatus.APPROVED.getCode());
        List<School> schools = baseMapper.selectList(schoolQuery);
        return SchoolConvert.INSTANCE.convertList(schools);
    }

    @Override
    public List<SchoolDTO> getSchoolListByIds(Set<Long> ids) {
        List<School> schoolList = baseMapper.selectList(new LambdaUpdateWrapper<School>().in(School::getId, ids));
        return SchoolConvert.INSTANCE.convertList(schoolList);
    }

    @Override
    public SchoolDTO getSchoolDetail(Long schoolId) {
        Map<Long, SchoolDTO> schoolMap = DataCacheUtil.getSchoolMap();
        if (schoolMap.containsKey(schoolId)) {
            return schoolMap.get(schoolId);
        } else {
            return SchoolConvert.INSTANCE.convert(baseMapper.selectById(schoolId));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public School saveSchool(SchoolReqVO reqVO) {
        School school = new School();
        // 新增学校区域编码校验
        SysAreaDTO area = areaUtil.getArea(reqVO.getAreaCode());
        Assert.isTrue(area.getType() == 2, "区域信息错误");

        School exist = baseMapper.selectOne(new QueryWrapper<School>().eq("name", reqVO.getName()).eq("area_code", reqVO.getAreaCode()));
        Assert.isNull(exist, "该区域相同名称学校已存在");

        //新增数据默认为待审核状态-暂时设置为已通过
        school.setApproveStatus(ApproveStatus.APPROVED.getCode());
        school.setName(reqVO.getName());
        school.setAreaCode(reqVO.getAreaCode());
        baseMapper.insert(school);
        return school;

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateSchool(SchoolReqVO reqVO) {
        School school = baseMapper.selectById(reqVO.getId());
        Assert.notNull(school, "未查询到学校数据");

        School exist = baseMapper.selectOne(new QueryWrapper<School>().eq("name", reqVO.getName()).eq("area_code", reqVO.getAreaCode()));
        if (Objects.nonNull(exist)) {
            Assert.isTrue(exist.getId().equals(school.getId()), "该区域相同名称学校已存在");
        }

        school.setName(reqVO.getName());
        school.setAreaCode(reqVO.getAreaCode());
        baseMapper.updateById(school);
    }

    @Override
    public List<BatchOperationTipDTO> deleteSchool(Set<Long> ids) {
        List<BatchOperationTipDTO> resultMap = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(ids)) {
            for (Long id : ids) {
                School school = baseMapper.selectById(id);
                if (Objects.isNull(school)) {
                    resultMap.add(new BatchOperationTipDTO(id, "无效id"));
                    continue;
                }
                baseMapper.deleteById(id);
                //删除校管理员
                sysUserService.deleteSchoolManagerBySchool(id);
                //删除校职员
                schoolTeacherService.deleteBySchoolId(id);
                //删除校心理老师
                schoolStaffService.deleteBySchoolId(id);
                //删除班级
                clazzService.deleteBySchoolId(id);
                //删除学生极其关联信息
                studentBaseInfoService.deleteBySchoolId(id);
                //删除家长
                parentService.deleteBySchoolId(id);
                //删除用户
                sysUserService.deleteBySchoolId(id);
                //更新学校缓存数据
                DataCacheUtil.removeSchoolCache(id);
            }
        }
        return resultMap;
    }

    @Override
    public void deleteByAreaCode(Integer areaCode) {
        Set<Long> ids = schoolMapper.getIdByAreaCode(areaCode);
        deleteSchool(ids);
    }

    @Override
    public List<Map<String, Object>> getSchoolStatistics() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int year = cal.get(Calendar.YEAR);
        String date = year + "-01";
        return baseMapper.getSchoolStatistics(date);
    }
}
