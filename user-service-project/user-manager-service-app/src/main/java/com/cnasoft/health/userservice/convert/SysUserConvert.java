package com.cnasoft.health.userservice.convert;

import com.cnasoft.health.common.dto.SysUserDTO;
import com.cnasoft.health.common.model.CommonModel;
import com.cnasoft.health.userservice.feign.dto.AreaManagerCreateReqVO;
import com.cnasoft.health.userservice.feign.dto.AreaManagerUpdateReqVO;
import com.cnasoft.health.userservice.feign.dto.AreaStaffReqVO;
import com.cnasoft.health.userservice.feign.dto.ParentReqVO;
import com.cnasoft.health.userservice.feign.dto.SchoolManagerCreateReqVO;
import com.cnasoft.health.userservice.feign.dto.SchoolManagerUpdateReqVO;
import com.cnasoft.health.userservice.feign.dto.SchoolStaffReqVO;
import com.cnasoft.health.userservice.feign.dto.SchoolTeacherReqVO;
import com.cnasoft.health.userservice.feign.dto.StudentBaseInfoRespVO;
import com.cnasoft.health.userservice.feign.dto.StudentSaveVO;
import com.cnasoft.health.userservice.feign.dto.SysUserCreateReqVO;
import com.cnasoft.health.userservice.feign.dto.SysUserUpdateReqVO;
import com.cnasoft.health.userservice.feign.dto.TestManagerReqVO;
import com.cnasoft.health.userservice.model.Parent;
import com.cnasoft.health.userservice.model.StudentAdditionalInfo;
import com.cnasoft.health.userservice.model.StudentBaseInfo;
import com.cnasoft.health.userservice.model.SysUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author cnasoft
 * @date 2020/8/12 20:15
 */
@Mapper
public interface SysUserConvert {
    SysUserConvert INSTANCE = Mappers.getMapper(SysUserConvert.class);

    /**
     * 实体对象转换为用户数据传输对象
     *
     * @param sysUserDO 实体对象
     * @return 数据对象
     */
    @Mapping(source = "id", target = "idStr")
    @Mapping(source = "roleCode", target = "presetRoleCode")
    SysUserDTO convert(SysUser sysUserDO);

    /**
     * 实体集合转换为数据传输对象
     *
     * @param list 列表
     * @return 数据对象列表
     */
    List<SysUserDTO> convertList(List<SysUser> list);

    /**
     * 添加用户请求数据转换为实体类
     *
     * @param sysUserCreateReqVO 请求对象
     * @return 用户实体
     */
    @Mapping(target = "openId", ignore = true)
    @Mapping(target = "key", ignore = true)
    @Mapping(target = "sex", source = "sex", qualifiedByName = "toSexEnum")
    SysUser convertVO(SysUserCreateReqVO sysUserCreateReqVO);

    /**
     * 修改用户请求数据转换为实体类
     *
     * @param sysUserUpdateReqVO 请求对象
     * @return 用户实体
     */
    @Mapping(target = "openId", ignore = true)
    @Mapping(target = "key", ignore = true)
    SysUser convertVO(SysUserUpdateReqVO sysUserUpdateReqVO);

    /**
     * 添加校级管理员请求数据转换为实体类
     *
     * @param managerCreateReqVO 校管理员请求对象
     * @return 用户对象
     */
    @Mapping(target = "openId", ignore = true)
    @Mapping(target = "key", ignore = true)
    SysUser convertVO(SchoolManagerCreateReqVO managerCreateReqVO);

    /**
     * 测试管理员请求数据转换为实体类
     *
     * @param testManagerReqVO 测试管理员请求对象
     * @return 用户对象
     */
    @Mapping(target = "openId", ignore = true)
    @Mapping(target = "key", ignore = true)
    SysUser convertVO(TestManagerReqVO testManagerReqVO);

    /**
     * 修改校级管理员请求数据转换为实体类
     *
     * @param managerUpdateReqVO 校管理员请求对象
     * @return 用户对象
     */
    @Mapping(target = "openId", ignore = true)
    @Mapping(target = "key", ignore = true)
    SysUser convertVO(SchoolManagerUpdateReqVO managerUpdateReqVO);

    /**
     * 添加区域管理员请求数据转换为实体类
     *
     * @param managerCreateReqVO 校管理员请求对象
     * @return 用户对象
     */
    @Mapping(target = "openId", ignore = true)
    @Mapping(target = "key", ignore = true)
    SysUser convertVO(AreaManagerCreateReqVO managerCreateReqVO);

    /**
     * 修改区域管理员请求数据转换为实体类
     *
     * @param managerCreateReqVO 校管理员请求对象
     * @return 用户对象
     */
    @Mapping(target = "openId", ignore = true)
    @Mapping(target = "key", ignore = true)
    SysUser convertVO(AreaManagerUpdateReqVO managerCreateReqVO);

    /**
     * 请求对象转实体对象
     *
     * @param teacherReqVO 校心理老师
     * @return 用户对象
     */
    SysUser convertWithSchoolTeacher(SchoolTeacherReqVO teacherReqVO);

    /**
     * 请求对象转实体对象
     *
     * @param staffReqVO 区域职员
     * @return 用户对象
     */
    SysUser convertWithAreaStaff(AreaStaffReqVO staffReqVO);

    /**
     * 添加学生请求数据转换为用户实体类
     *
     * @param studentCreateVO 学生请求对象
     * @return 用户对象
     */
    SysUser convertVO(StudentSaveVO studentCreateVO);

    /**
     * 转化为学生基本信息
     *
     * @param studentCreateVO 学生请求对象
     * @return 学生基本信息
     */
    StudentBaseInfo convertStudentVO(StudentSaveVO studentCreateVO);

    /**
     * 转化为学生补充信息实体
     *
     * @param studentCreateVO 学生请求对象
     * @return 学生补充信息
     */
    StudentAdditionalInfo convertAdditionalVO(StudentSaveVO studentCreateVO);

    /**
     * 转化家长请求实体
     *
     * @param parentReqVO 家长请求对象
     * @return 用户实体
     */
    SysUser convertVO(ParentReqVO parentReqVO);

    /**
     * 数据对象转换为实体对象
     *
     * @param parentReqVO 请求对象
     * @return 家长实体
     */
    Parent convertParentVO(ParentReqVO parentReqVO);

    /**
     * 请求对象转实体对象
     *
     * @param staffReqVO 校心理老师
     * @return 用户对象
     */
    SysUser convertWithSchoolStaff(SchoolStaffReqVO staffReqVO);

    /**
     * 数据对象转换为实体对象
     *
     * @param baseInfo 学生基本信息
     * @return 实体对象
     */
    StudentSaveVO convert(StudentBaseInfo baseInfo);

    /**
     * 数据对象转换为实体对象
     *
     * @param sysUser 账号信息
     * @return 实体对象
     */
    List<CommonModel> convertCommonModel(List<SysUser> sysUser);

    /**
     * 转化为学生基本信息实体对象
     *
     * @param studentBaseInfo 学生请求对象
     * @return 学生基本信息
     */
    StudentBaseInfoRespVO convertStudentBaseVO(StudentBaseInfo studentBaseInfo);
}
