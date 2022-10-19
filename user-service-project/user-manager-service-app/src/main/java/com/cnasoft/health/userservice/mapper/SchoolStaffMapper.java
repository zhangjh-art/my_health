package com.cnasoft.health.userservice.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.dto.CommonDTO;
import com.cnasoft.health.common.dto.SchoolStaffDTO;
import com.cnasoft.health.common.dto.SchoolTeacherStaffDTO;
import com.cnasoft.health.common.vo.UserClazzVO;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.feign.dto.StaffMentalFileVO;
import com.cnasoft.health.userservice.feign.dto.TaskUserReqVO;
import com.cnasoft.health.userservice.feign.dto.TaskUserResVO;
import com.cnasoft.health.userservice.model.SchoolStaff;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lgf
 * @date 2022/3/29.
 */
public interface SchoolStaffMapper extends SuperMapper<SchoolStaff> {
    /**
     * 发起任务时查询校教职工用户
     *
     * @param page  分页参数
     * @param query 查询实体
     * @param key   秘钥
     * @return
     */
    List<TaskUserResVO> getTaskUsers(Page<TaskUserResVO> page, @Param("query") TaskUserReqVO query, @Param("key") String key);

    /**
     * 根据用户名和学校id及角色编码查询校教职工
     *
     * @param schoolId  学校id
     * @param roleCode  角色编码
     * @param usernames 用户名列表
     * @param key       秘钥
     * @return
     */
    List<SchoolStaff> selectBySchoolStaff(@Param("schoolId") Long schoolId, @Param("roleCode") String roleCode, @Param("usernames") List<String> usernames,
        @Param("key") String key);

    /**
     * 分页查询校教职工列表
     *
     * @param page   分页参数
     * @param params 查询条件
     * @param key    key
     * @return 校教职工列表
     */
    List<SchoolStaff> findList(Page<SchoolStaff> page, @Param("p") Map<String, Object> params, @Param("key") String key);

    /**
     * 根据用户id查询教职工详情
     *
     * @param userId 用户id
     * @param key    key
     * @return 教职工信息
     */
    SchoolStaff findByUserId(@Param("userId") Long userId, @Param("key") String key);

    /**
     * 根据教职工id和学校id查询教职工详情
     *
     * @param id       教职工id
     * @param schoolId 学校id
     * @param key      key
     * @return 教职工信息
     */
    SchoolStaff findByIdAndSchoolId(@Param("id") Long id, @Param("schoolId") Long schoolId, @Param("key") String key);

    /**
     * 所有班主任列表
     *
     * @param schoolId 学校id
     * @param type     1：教职工；2：班主任；3：领导
     * @param key      秘钥
     * @return 班主任列表
     */
    List<CommonDTO> headerTeacherList(@Param("schoolId") Long schoolId, @Param("type") int type, @Param("key") String key);

    /**
     * 校心理老师是否有查看测评结构数据权限
     *
     * @param schoolId
     * @param userId
     * @return
     */
    Integer schoolTaskInScope(@Param("schoolId") Long schoolId, @Param("userId") Long userId);

    /**
     * 根据班主任手机号查询校教职工id列表
     *
     * @param mobiles  手机号列表
     * @param schoolId 学校id
     * @param key      key
     * @return
     */
    List<SchoolStaff> getSchoolStaffIdsByMobiles(@Param("mobiles") List<String> mobiles, @Param("schoolId") Long schoolId, @Param("key") String key);

    /**
     * 获取学校职员下拉列表
     *
     * @param schoolId 学校id
     * @param userName 姓名
     * @return 区域职员下拉列表
     */
    List<Map<String, Object>> getSelectList(@Param("schoolId") Long schoolId, @Param("userName") String userName, @Param("department") String department, @Param("key") String key);

    /**
     * 查询心理档案列表细腻
     *
     * @param page
     * @param param
     * @param key
     * @return
     */
    List<StaffMentalFileVO> listMentalFile(Page<StaffMentalFileVO> page, @Param("param") Map<String, Object> param, @Param("key") String key);

    /**
     * 根据学校id和部门code列表获取教职工（包含心理老师）用户id列表
     *
     * @param schoolId        学校i
     * @param departmentCodes 部门列表
     * @return 用户id列表
     */
    List<UserClazzVO> getSchoolStaffUserIdBySchoolAndDepartmentCode(@Param("schoolId") Long schoolId, @Param("departmentCodes") List<String> departmentCodes);

    /**
     * 根据用户id获取校教职工数据
     *
     * @param userId 用户id
     * @return 校教职工数据
     */
    SchoolStaffDTO findSchoolStaffInfo(@Param("userId") Long userId);

    /**
     * 根据用户id查询校心理老师和教职工数据
     *
     * @param userIds 用户id列表
     * @param key     秘钥
     * @return 校心理老师和教职工数据
     */
    List<SchoolTeacherStaffDTO> findSchoolTeacherStaffList(@Param("userIds") Set<Long> userIds, @Param("key") String key);
}
