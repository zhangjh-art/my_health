package com.cnasoft.health.userservice.service;

import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.CommonDTO;
import com.cnasoft.health.common.dto.SchoolStaffDTO;
import com.cnasoft.health.common.dto.SchoolTeacherStaffDTO;
import com.cnasoft.health.common.service.ISuperService;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.common.vo.UserClazzVO;
import com.cnasoft.health.userservice.feign.dto.SchoolStaffReqVO;
import com.cnasoft.health.userservice.feign.dto.SchoolStaffRespVO;
import com.cnasoft.health.userservice.feign.dto.StaffMentalFileVO;
import com.cnasoft.health.userservice.feign.dto.UserRespVO;
import com.cnasoft.health.userservice.model.SchoolStaff;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lgf
 */
public interface ISchoolStaffService extends ISuperService<SchoolStaff> {

    /**
     * 添加校教职工
     *
     * @param staff 教职工/领导对象
     * @throws Exception 异常
     */
    SchoolStaff add(SchoolStaffReqVO staff) throws Exception;

    /**
     * 修改校教职工
     *
     * @param staff 教职工/领导对象
     * @throws Exception 异常
     */
    void update(SchoolStaffReqVO staff) throws Exception;

    /**
     * 批量删除校教职工
     *
     * @param ids id列表
     * @return 受影响的行数
     * @throws Exception 异常
     */
    List<BatchOperationTipDTO> delete(Set<Long> ids) throws Exception;

    /**
     * 获取所有
     * 查询条件：①ID/工号/手机号/姓名 ②部门 ③账号状态
     *
     * @param params 参数
     * @return 分页对象
     */
    PageResult<SchoolStaffRespVO> findList(Map<String, Object> params);

    /**
     * 班主任列表
     *
     * @return 列表数据
     */
    List<CommonDTO> headerTeacherList();

    /**
     * 获取教职工详情
     *
     * @param userId 用户id
     * @return 教职工信息
     */
    SchoolStaffRespVO findByUserId(Long userId);

    /**
     * 修改当前登录用户信息
     *
     * @param staffReqVO 教职工信息
     */
    void updateCurrentSchoolStaff(SchoolStaffReqVO staffReqVO) throws Exception;

    /**
     * 根据学校id删除
     *
     * @param schoolId 学校id
     */
    void deleteBySchoolId(Long schoolId);

    /**
     * 教职工下拉列表
     *
     * @param schoolId 查询条件
     * @param userName 家长名称
     * @return 家长列表
     */
    List<Map<String, Object>> getSelectList(Long schoolId, String userName, String department);

    /**
     * 获取校教职工心理档案列表
     *
     * @param params
     * @return
     */
    PageResult<StaffMentalFileVO> listMentalFile(Map<String, Object> params) throws IllegalAccessException;

    /**
     * 校职员详情,包含校心理教师信息
     *
     * @param userId
     * @return
     */
    UserRespVO findUnionUser(Long userId);

    /**
     * 根据学校id和部门code列表获取教职工（包含心理老师）用户id列表
     *
     * @param schoolId        学校i
     * @param departmentCodes 部门列表
     * @return 用户id列表
     */
    List<UserClazzVO> getSchoolStaffUserIdBySchoolAndDepartmentCode(Long schoolId, List<String> departmentCodes);

    /**
     * 根据用户id获取校教职工数据
     *
     * @param userId 用户id
     * @return 校教职工数据
     */
    SchoolStaffDTO findSchoolStaffInfo(Long userId);

    /**
     * 根据用户id查询校心理老师和教职工数据
     *
     * @param userIds 用户id列表
     * @return 校心理老师和教职工数据
     */
    List<SchoolTeacherStaffDTO> findSchoolTeacherStaffList(Set<Long> userIds);
}
