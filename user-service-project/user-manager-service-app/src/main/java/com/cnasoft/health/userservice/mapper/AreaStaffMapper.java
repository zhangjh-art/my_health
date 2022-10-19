package com.cnasoft.health.userservice.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.dto.AreaStaffDTO;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.feign.dto.AreaStaffReqVO;
import com.cnasoft.health.userservice.feign.dto.StaffMentalFileVO;
import com.cnasoft.health.userservice.feign.dto.TaskUserReqVO;
import com.cnasoft.health.userservice.feign.dto.TaskUserResVO;
import com.cnasoft.health.userservice.model.AreaStaff;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @Created by lgf on 2022/3/29.
 */
public interface AreaStaffMapper extends SuperMapper<AreaStaff> {
    /**
     * 任务查询区域职员用户
     *
     * @param page  分页
     * @param query 查询
     * @return
     */
    List<TaskUserResVO> getTaskUsers(Page<TaskUserResVO> page, @Param("query") TaskUserReqVO query, @Param("key") String key);

    /**
     * 根据区域编码、角色编码、用户名列表查询区域职员
     *
     * @param areaCode
     * @param roleCode
     * @param usernames
     * @param key
     * @return
     */
    List<AreaStaff> selectByAreaStaff(@Param("areaCode") Integer areaCode, @Param("roleCode") String roleCode, @Param("usernames") List<String> usernames,
        @Param("key") String key);

    /**
     * 根据UserId查询区域职员列表信息
     *
     * @param userIds
     * @return list
     */
    List<AreaStaffReqVO> selectAreaStaffByUserIds(@Param("userIds") List<Long> userIds);

    /**
     * 分页查询区域职员列表
     *
     * @param page
     * @param params
     * @param key
     * @return
     */
    List<AreaStaff> findList(Page<AreaStaff> page, @Param("p") Map<String, Object> params, @Param("key") String key);

    /**
     * 根据用户id查询区域职员详情
     *
     * @param userId 用户id
     * @param key    key
     * @return 教职工信息
     */
    AreaStaff findByUserId(@Param("userId") Long userId, @Param("key") String key);

    /**
     * 区域心理教研员是否有查看该用户测评结果的数据权限
     *
     * @param areaCode 区域心理教研员区域码值
     * @param userId   测评记录的用户id
     * @return
     */
    Integer areaTaskInScope(@Param("areaCode") Integer areaCode, @Param("userId") Long userId);

    /**
     * 查看心理档案列表
     *
     * @param page
     * @param param
     * @param key
     * @return
     */
    List<StaffMentalFileVO> listMentalFile(Page<StaffMentalFileVO> page, @Param("param") Map<String, Object> param, @Param("key") String key);

    /**
     * 获取区域职员下拉列表
     *
     * @param areaCode 区域编码
     * @param userName 姓名
     * @return 区域职员下拉列表
     */
    List<Map<String, Object>> getSelectList(@Param("areaCode") Integer areaCode, @Param("userName") String userName, @Param("key") String key);

    /**
     * 获取区域职员信息
     *
     * @param userId 用户id
     * @return 区域职员信息
     */
    AreaStaffDTO findAreaStaffInfo(@Param("userId") Long userId);
}
