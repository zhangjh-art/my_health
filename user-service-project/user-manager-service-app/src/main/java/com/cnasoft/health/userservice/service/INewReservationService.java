package com.cnasoft.health.userservice.service;

import com.cnasoft.health.common.service.ISuperService;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.feign.dto.NewReservationReqVO;
import com.cnasoft.health.userservice.feign.dto.NewReservationRespVO;
import com.cnasoft.health.userservice.feign.dto.SupplementReservationReqVO;
import com.cnasoft.health.userservice.model.NewReservation;

import java.util.List;
import java.util.Map;

/**
 * @author zjh
 * @date 2022/7/19
 */
public interface INewReservationService extends ISuperService<NewReservation> {
    /**
     * 新建预约
     *
     * @param vo            请求数据
     * @param isSubstituted 是否代预约
     */
    Long create(NewReservationReqVO vo, Integer isSubstituted) throws Exception;

    Long createLocked(NewReservationReqVO vo, Integer isSubstituted) throws Exception;

    /**
     * 补充预约
     *
     * @param vo 请求数据
     */
    void supplementCreate(SupplementReservationReqVO vo) throws Exception;

    /**
     * 编辑预约
     *
     * @param vo 请求数据
     */
    void update(NewReservationReqVO vo) throws Exception;

    void updateLocked(NewReservationReqVO vo) throws Exception;

    /**
     * 获取预约列表
     *
     * @param id 预约id
     * @return 预约详情
     */
    NewReservationRespVO get(Long id);

    /**
     * 代预约查询预约用户
     *
     * @param params 查询条件
     * @return 可预约用户列表
     */
    List<Map<String, Object>> getReservationUserList(Map<String, Object> params);

    /**
     * 获取预约列表
     *
     * @param params 查询条件
     * @return 预约列表
     */
    PageResult<NewReservationRespVO> list(Map<String, Object> params, boolean listByTeacher);

    /**
     * 获取预约总览数据
     *
     * @param year  年份
     * @param month 月份
     * @return 月度总览列表
     */
    Map<String, List<Map<String, Object>>> getStatisticalData(Integer year, Integer month);

    /**
     * 更新预约状态
     *
     * @param id     预约id
     * @param status 状态
     */
    void updateStatus(Long id, Integer status, String remark, String cancelOtherReason) throws Exception;

    void updateStatusLocked(Long id, Integer status, String remark, String cancelOtherReason) throws Exception;

    /**
     * 查询心理老师待确认预约
     */
    PageResult<NewReservationRespVO> getToConfirmedTask();

    /**
     * 查询用户已确认预约
     */
    PageResult<NewReservationRespVO> getConfirmedTask();

    /**
     * 补充预约，事务数据，状态修改为业务可用状态
     * @param params
     * @param transactionId
     */
    void fullReservationSupplement(Map<String, String> params, String transactionId);
}
