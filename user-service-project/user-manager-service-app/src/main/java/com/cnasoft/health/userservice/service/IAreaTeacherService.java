package com.cnasoft.health.userservice.service;

import com.cnasoft.health.common.dto.AreaTeacherDTO;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.CommonDTO;
import com.cnasoft.health.common.service.ISuperService;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.feign.dto.AreaTeacherReqVO;
import com.cnasoft.health.userservice.feign.dto.AreaTeacherRespVO;
import com.cnasoft.health.userservice.model.AreaTeacher;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 区域心理教研员
 *
 * @author lqz
 * @date 2022/4/15
 */
public interface IAreaTeacherService extends ISuperService<AreaTeacher> {

    /**
     * 根据区域id获取心理教研员列表
     *
     * @param params
     * @return PageResult
     */
    PageResult<AreaTeacherRespVO> findList(Map<String, Object> params);

    /**
     * 添加区域心理教研员
     *
     * @param teacherReqVO
     * @throws Exception
     */
    AreaTeacher saveAreaTeacher(AreaTeacherReqVO teacherReqVO) throws Exception;

    /**
     * 更新区域心理教研员信息
     *
     * @param teacherReqVO
     * @throws Exception
     */
    AreaTeacher updateAreaTeacher(AreaTeacherReqVO teacherReqVO) throws Exception;

    /**
     * 逻辑删除/批量删除区域心理教研员
     *
     * @param ids 区域心理教研员id集合
     * @return int
     */
    List<BatchOperationTipDTO> deleteAreaTeacher(@Param("ids") Set<Long> ids);

    /**
     * 获取区域心理教研员详情
     *
     * @param userId 用户id
     * @return 区域心理教研员详情
     */
    AreaTeacherRespVO findByUserId(Long userId);

    /**
     * 修改当前登录用户信息
     *
     * @param teacherReqVO
     */
    void updateCurrentAreaTeacher(AreaTeacherReqVO teacherReqVO) throws Exception;

    /**
     * 根据区域编码删除
     *
     * @param areaCode 区域编码
     */
    void deleteByAreaCode(Integer areaCode);

    /**
     * 重新指派任务承接人
     *
     * @param areaCode 区域编码
     * @param userId   承接人用户id
     */
    void reconfirmTaskHandler(Integer areaCode, Long userId);

    /**
     * 通过时段获取空闲咨询师列表
     *
     * @param areaCode  区域编码
     * @param weekDay   周几
     * @param startTime 开始时间
     * @param endTime   结束时间
     */
    List<Map<String, Object>> getSelectListByReservationConfig(Integer areaCode, Integer weekDay, String startTime, String endTime, Date date);

    /**
     * 根据区域code查找区域承接任务的心理教研员的用户id
     *
     * @param areaCode 区域编码
     * @return 用户id
     */
    Long findTaskHandlerIdByAreaCode(Integer areaCode);

    /**
     * 根据用户id查询区域心理教研员信息
     *
     * @param userId 用户id
     * @return 区域心理教研员信息
     */
    AreaTeacherDTO findAreaTeacherInfo(Long userId);

    /**
     * 查询区域心理教研员数据
     *
     * @param areaCode 区域编码
     * @return 区域心理教研员数据
     */
    List<CommonDTO> getAreaPsychoTeacher(Integer areaCode);
}
