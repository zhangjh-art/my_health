package com.cnasoft.health.userservice.service;

import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.CommonDTO;
import com.cnasoft.health.common.dto.SchoolTeacherDTO;
import com.cnasoft.health.common.service.ISuperService;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.feign.dto.SchoolTeacherReqVO;
import com.cnasoft.health.userservice.feign.dto.SchoolTeacherRespVO;
import com.cnasoft.health.userservice.model.SchoolTeacher;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 校心理老师
 *
 * @author lgf
 * @date 2022/3/29
 */
public interface ISchoolTeacherService extends ISuperService<SchoolTeacher> {

    /**
     * 获取所有
     * 查询条件：①ID/工号/手机号/姓名 ②部门 ③账号状态
     *
     * @param params 参数
     * @return 分页对象
     */
    PageResult<SchoolTeacherRespVO> findList(Map<String, Object> params);

    /**
     * 添加校心理老师
     *
     * @param teacher 老师对象
     * @return
     * @throws Exception 异常
     */
    SchoolTeacher add(SchoolTeacherReqVO teacher) throws Exception;

    /**
     * 校心理老师修改
     *
     * @param teacher 老师对象
     * @throws Exception 异常
     */
    SchoolTeacher update(SchoolTeacherReqVO teacher) throws Exception;

    /**
     * 删除校心理老师
     *
     * @param ids 老师ID列表
     * @return 受影响的行数
     * @throws Exception 异常
     */
    List<BatchOperationTipDTO> delete(Set<Long> ids) throws Exception;

    /**
     * 获取校心理教师详情
     *
     * @param userId 用户id
     * @return 心理教师详情
     */
    SchoolTeacherRespVO findByUserId(Long userId);

    /**
     * 更新当前登录用户信息
     *
     * @param teacherReqVO 校心理教师信息
     * @throws Exception 异常信息
     */
    void updateCurrentSchoolTeacher(SchoolTeacherReqVO teacherReqVO) throws Exception;

    /**
     * 根据学校id删除
     *
     * @param schoolId 学校id
     */
    void deleteBySchoolId(Long schoolId);

    /**
     * 重新指派任务承接人
     *
     * @param schoolId 学校id
     * @param userId   承接人用户id
     */
    void reconfirmTaskHandler(Long schoolId, Long userId);

    /**
     * 通过时段获取空闲咨询师列表
     *
     * @param schoolId  学校id
     * @param weekDay   周几
     * @param startTime 开始时间
     * @param endTime   结束时间
     */
    List<Map<String, Object>> getSelectListByReservationConfig(Long schoolId, Integer weekDay, String startTime, String endTime, Date date);

    /**
     * 根据学校id查找承接任务的学校心理老师的用户id
     *
     * @param schoolId 学校id
     * @return 用户id
     */
    Long findTaskHandlerIdBySchoolId(Long schoolId);

    /**
     * 获取心理老师的学校id
     *
     * @param userId 用户id
     * @return 学校id
     */
    Long getSchoolPsychoTeacherSchoolId(Long userId);

    /**
     * 根据用户id获取校心理老师数据
     *
     * @param userId 用户id
     * @return 校心理老师数据
     */
    SchoolTeacherDTO findSchoolTeacherInfo(Long userId);

    /**
     * 查询校心理老师数据
     *
     * @param schoolId 学校id
     * @return 校心理老师数据
     */
    List<CommonDTO> getSchoolPsychoTeacher(Long schoolId);
}
