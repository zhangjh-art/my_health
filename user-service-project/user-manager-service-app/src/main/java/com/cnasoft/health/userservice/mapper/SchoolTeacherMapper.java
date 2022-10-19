package com.cnasoft.health.userservice.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.dto.CommonDTO;
import com.cnasoft.health.common.dto.SchoolTeacherDTO;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.model.SchoolTeacher;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author by
 * @date 2022/3/29
 */
public interface SchoolTeacherMapper extends SuperMapper<SchoolTeacher> {

    /**
     * 根据校心理老师id和学校id查询校心理老师详情
     *
     * @param id       校心理老师id
     * @param schoolId 学校id
     * @param key      key
     * @return 教职工信息
     */
    SchoolTeacher findByIdAndSchoolId(@Param("id") Long id, @Param("schoolId") Long schoolId, @Param("key") String key);

    /**
     * 根据用户名和学校id查询校心理老师
     *
     * @param schoolId  学校id
     * @param usernames 用户名列表
     * @param key       key
     * @return 校心理老师列表
     */
    List<SchoolTeacher> selectBySchoolTeacher(@Param("schoolId") Long schoolId, @Param("usernames") List<String> usernames, @Param("key") String key);

    /**
     * 分页查询校心理老师列表
     *
     * @param page   分页参数
     * @param params 查询条件
     * @param key    key
     * @return 校心理老师列表
     */
    List<SchoolTeacher> findList(Page<SchoolTeacher> page, @Param("p") Map<String, Object> params, @Param("key") String key);

    /**
     * 将其他用户的是否承接任务属性设置为false
     *
     * @param schoolId 学校id
     * @param id       校心理老师id
     * @return 受影响的行数
     */
    boolean setAcceptTaskForOtherTeacher(@Param("schoolId") Long schoolId, @Param("id") Long id);

    /**
     * 检查指定学校是否存在任务承接人
     *
     * @param schoolId 学校id
     * @return 任务承接人id
     */
    Long getAcceptTaskId(@Param("schoolId") Long schoolId);

    /**
     * 根据学校id查找承接任务的学校心理老师的用户id
     *
     * @param schoolId 学校id
     * @return 用户id
     */
    Long findTaskHandlerIdBySchoolId(@Param("schoolId") Long schoolId);

    /**
     * 获取心理老师的学校id
     *
     * @param userId 用户id
     * @return 学校id
     */
    Long getSchoolPsychoTeacherSchoolId(@Param("userId") Long userId);

    /**
     * 根据用户id查询校心理教师详情
     *
     * @param userId 用户id
     * @param key    key
     * @return 心理教师信息
     */
    SchoolTeacher findByUserId(@Param("userId") Long userId, @Param("key") String key);

    /**
     * 通过时段获取空闲咨询师列表
     *
     * @param schoolId  区域编码
     * @param weekDay   周几
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 心理教研员列表信息
     */
    List<Map<String, Object>> selectDropDownList(@Param("schoolId") Long schoolId, @Param("weekDay") Integer weekDay, @Param("startTime") String startTime,
        @Param("endTime") String endTime, @Param("date") Date date, @Param("key") String key);

    /**
     * 根据用户id获取校心理老师数据
     *
     * @param userId 用户id
     * @return 校心理老师数据
     */
    SchoolTeacherDTO findSchoolTeacherInfo(@Param("userId") Long userId);

    /**
     * 查询校心理老师数据
     *
     * @param schoolId 学校id
     * @param key      秘钥
     * @return 校心理老师数据
     */
    List<CommonDTO> getSchoolPsychoTeacher(@Param("schoolId") Long schoolId, @Param("key") String key);

    /**
     * 修改数据状态为正常业务可用状态
     *
     * @param id 业务id
     */
    void fullData(@Param("id") Long id);
}
