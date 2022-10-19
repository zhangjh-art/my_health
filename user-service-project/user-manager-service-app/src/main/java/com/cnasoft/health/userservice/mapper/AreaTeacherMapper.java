package com.cnasoft.health.userservice.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.dto.AreaTeacherDTO;
import com.cnasoft.health.common.dto.CommonDTO;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.feign.dto.AreaTeacherRespVO;
import com.cnasoft.health.userservice.model.AreaTeacher;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author lgf
 * @date 2022/3/29
 */
public interface AreaTeacherMapper extends SuperMapper<AreaTeacher> {

    /**
     * 根据区域编码、用户名列表查询区域心理教研员
     *
     * @param areaCode  区域编码
     * @param roleCode  角色编码
     * @param usernames 用户名列表
     * @param key       key
     * @return 区域心理教研员列表
     */
    List<AreaTeacher> selectByAreaTeacher(@Param("areaCode") Integer areaCode, @Param("roleCode") String roleCode, @Param("usernames") List<String> usernames,
        @Param("key") String key);

    /**
     * 根据userIds查询区域心理教研员的用户信息
     *
     * @param userIds 区域心理教研员id
     * @return list
     */
    List<AreaTeacherRespVO> selectAreaTeacherByUserIds(@Param("userIds") List<Long> userIds);

    /**
     * 将其他用户的是否承接任务属性设置为false
     *
     * @param areaCode 区域编码
     * @param id       区域心理教研员id
     * @return 受影响的行数
     */
    boolean setAcceptTaskForOtherTeacher(@Param("areaCode") Integer areaCode, @Param("id") Long id);

    /**
     * 检查指定区域是否存在任务承接人
     *
     * @param areaCode 区域编码
     * @return 任务承接人id
     */
    Long getAcceptTaskId(@Param("areaCode") Integer areaCode);

    /**
     * 根据区域code查找区域承接任务的心理教研员的用户id
     *
     * @param areaCode 区域编码
     * @return 用户id
     */
    Long findTaskHandlerIdByAreaCode(@Param("areaCode") Integer areaCode);

    /**
     * 分页查询区域心理教研员列表
     *
     * @param page   页数参数
     * @param params 查询参数
     * @param key    key
     * @return list 分页数据
     */
    List<AreaTeacher> findList(Page<AreaTeacher> page, @Param("p") Map<String, Object> params, @Param("key") String key);

    /**
     * 根据用户id查询区域心理教研员详情
     *
     * @param userId 用户id
     * @param key    key
     * @return 心理教研员信息
     */
    AreaTeacher findByUserId(@Param("userId") Long userId, @Param("key") String key);

    /**
     * 通过时段获取空闲咨询师列表
     *
     * @param areaCode  区域编码
     * @param weekDay   周几
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 心理教研员列表信息
     */
    List<Map<String, Object>> selectDropDownList(@Param("areaCode") Integer areaCode, @Param("weekDay") Integer weekDay, @Param("startTime") String startTime,
        @Param("endTime") String endTime, @Param("date") Date date, @Param("key") String key);

    /**
     * 根据用户id查询区域心理教研员信息
     *
     * @param userId 用户id
     * @return 区域心理教研员信息
     */
    AreaTeacherDTO findAreaTeacherInfo(@Param("userId") Long userId);

    /**
     * 查询区域心理教研员数据
     *
     * @param areaCode 区域编码
     * @param key      秘钥
     * @return 区域心理教研员数据
     */
    List<CommonDTO> getAreaPsychoTeacher(@Param("areaCode") Integer areaCode, @Param("key") String key);

    /**
     * 修改数据状态为正常业务可用状态
     *
     * @param id 业务id
     */
    void fullData(@Param("id") Long id);
}
