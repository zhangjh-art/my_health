package com.cnasoft.health.userservice.service;

import com.cnasoft.health.common.service.ISuperService;
import com.cnasoft.health.userservice.feign.dto.ReservationConfigReqVO;
import com.cnasoft.health.userservice.feign.dto.ReservationConfigRespVO;
import com.cnasoft.health.userservice.model.ReservationConfig;

import java.util.List;
import java.util.Map;

public interface IReservationConfigService extends ISuperService<ReservationConfig> {

    /**
     * 新建预约设置
     *
     * @param vo 请求数据
     */
    void create(ReservationConfigReqVO vo);

    /**
     * 编辑预约设置
     *
     * @param vo 请求数据
     */
    Map<String, List<Map<String, Object>>> update(ReservationConfigReqVO vo);

    /**
     * 获取预约设置详情
     *
     * @return 预约设置详情
     */
    ReservationConfigRespVO get();

    /**
     * 获取预约设置详情
     *
     * @return 预约设置详情
     */
    ReservationConfigRespVO get(Long userId);

    /**
     * 根据咨询师id获取设置详情
     *
     * @param userId 咨询师id
     * @return 预约设置详情
     */
    ReservationConfigRespVO getByUserId(Long userId);

    /**
     * 获取空闲咨询师
     *
     * @param params 查询条件
     * @return 空闲咨询师
     */
    List<Map<String, Object>> getAvailableTeachers(Map<String, Object> params);

    /**
     * 获取咨询师空闲时间端
     *
     * @param psychiatristId 咨询师id
     * @param date           查询日期
     * @return 空闲咨询师
     */
    List<Map<String, Object>> getAvailableTimeByTeacherId(Long psychiatristId, Long date);

    /**
     * 根据日期查询当天可预约时间段
     *
     * @param date 查询日期
     * @return 空闲咨询师
     */
    List<Map<String, Object>> getAvailableTimeByDate(Long date);
}
