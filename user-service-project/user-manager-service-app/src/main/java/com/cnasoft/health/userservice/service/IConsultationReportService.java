package com.cnasoft.health.userservice.service;

import com.cnasoft.health.common.service.ISuperService;
import com.cnasoft.health.evaluation.feign.dto.WarningRecordFeignDTO;
import com.cnasoft.health.userservice.feign.dto.ConsultationReportReqVO;
import com.cnasoft.health.userservice.feign.dto.ConsultationReportRespVO;
import com.cnasoft.health.userservice.model.ConsultationReport;
import com.cnasoft.health.userservice.model.NewReservation;

public interface IConsultationReportService extends ISuperService<ConsultationReport> {

    /**
     * 新建咨询报告
     *
     * @param vo 请求数据
     */
    void create(ConsultationReportReqVO vo) throws Exception;

    void createLocked(ConsultationReportReqVO vo) throws Exception;

    /**
     * rocket 事务消息执行本地事务，业务数据入库
     * @param report 业务数据
     * @param transactionId 消息唯一标识
     */
    void create(ConsultationReport report, String transactionId) throws Exception;

    void createLocked(ConsultationReport report, String transactionId) throws Exception;

    /**
     * 编辑咨询报告
     *
     * @param vo 请求数据
     */
    void update(ConsultationReportReqVO vo);

    /**
     * 获取咨询报告
     *
     * @param reservationId 预约id
     * @return 咨询报告详情
     */
    ConsultationReportRespVO get(Long reservationId);

    /**
     * 生成预警信息
     */
    WarningRecordFeignDTO addWarningRecord(NewReservation newReservation, ConsultationReport report);
}
