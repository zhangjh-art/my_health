package com.cnasoft.health.userservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cnasoft.health.common.dto.TransactionMsgDefinationDTO;
import com.cnasoft.health.common.enums.WarningLevelEnum;
import com.cnasoft.health.common.enums.WarningSourceEnum;
import com.cnasoft.health.common.enums.WarningStatus;
import com.cnasoft.health.common.exception.LockException;
import com.cnasoft.health.common.lock.IDistLock;
import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.common.util.JsonUtils;
import com.cnasoft.health.evaluation.feign.WarningFeign;
import com.cnasoft.health.evaluation.feign.dto.WarningRecordFeignDTO;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.constant.UserConstant;
import com.cnasoft.health.userservice.enums.ConsultResultEnum;
import com.cnasoft.health.userservice.enums.ConsultWarningLevelEnum;
import com.cnasoft.health.userservice.feign.dto.ConsultationReportReqVO;
import com.cnasoft.health.userservice.feign.dto.ConsultationReportRespVO;
import com.cnasoft.health.userservice.mapper.ConsultationReportMapper;
import com.cnasoft.health.userservice.mapper.RocketmqTransactionLogMapper;
import com.cnasoft.health.userservice.model.ConsultationReport;
import com.cnasoft.health.userservice.model.NewReservation;
import com.cnasoft.health.userservice.model.RocketmqTransactionLog;
import com.cnasoft.health.userservice.service.IConsultationReportService;
import com.cnasoft.health.userservice.service.INewReservationService;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Date;
import java.util.UUID;

import static com.cnasoft.health.common.exception.util.ServiceExceptionUtil.exception;

@Service
public class ConsultationReportImpl extends SuperServiceImpl<ConsultationReportMapper, ConsultationReport>
    implements IConsultationReportService {

    @Resource
    private INewReservationService reservationService;
    @Resource
    private IConsultationReportService consultationReportService;
    @Resource
    WarningFeign warningFeign;
    @Resource
    private IDistLock lock;
    @Resource
    RocketMQTemplate rocketMQTemplate;
    @Resource
    RocketmqTransactionLogMapper rocketmqTransactionLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createLocked(ConsultationReportReqVO vo) throws Exception {
        Object locker = null;
        try {
            String lockKey = UserConstant.LOCK_KEY_RESERVATION + vo.getReservationId();
            locker = lock.tryLock(lockKey);
            consultationReportService.create(vo);
        } finally {
            lock.unlock(locker);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ConsultationReportReqVO vo) throws Exception {
        ConsultationReport report = new ConsultationReport();
        checkIsEarlyWarning(vo);
        BeanUtil.copyProperties(vo, report);

        NewReservation newReservation = reservationService.getById(report.getReservationId());
        Integer isEarlyWarning = report.getIsEarlyWarning();
        if (isEarlyWarning != null && isEarlyWarning != 0) {
            report.setIsDeleted(true);
            baseMapper.insert(report);
            WarningRecordFeignDTO feignDTO = addWarningRecord(newReservation, report);
            String transactionId = UUID.randomUUID().toString();
            TransactionMsgDefinationDTO msgDefinationDTO = new TransactionMsgDefinationDTO();
            msgDefinationDTO.setDestClass(this.getClass());
            msgDefinationDTO.setMethod("createLocked");
            msgDefinationDTO.setArg(report);
            TransactionSendResult sendResult = rocketMQTemplate
                .sendMessageInTransaction(Constant.ADD_WARNING_GROUP, Constant.ADD_WARNING_TOPIC,
                    MessageBuilder.withPayload(feignDTO).setHeader(RocketMQHeaders.TRANSACTION_ID, transactionId)
                        .build(), msgDefinationDTO);
            if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                //mq 异常
                throw exception("系统繁忙,请稍后重试");
            }
            if (LocalTransactionState.ROLLBACK_MESSAGE.equals(sendResult.getLocalTransactionState())) {
                //数据库异常
                throw exception("系统繁忙,请稍后重试");
            }
        } else {
            baseMapper.insert(report);
            if (newReservation.getStatus() != 2) {
                reservationService.updateStatus(newReservation.getId(), 2, null, null);
            }
        }
    }

    //这里一定要加本地事务，事务确保业务消息和mqlog入库的原子性
    @Override
    public void createLocked(ConsultationReport report, String transactionId) throws Exception {
        Object locker = null;
        try {
            String lockKey = UserConstant.LOCK_KEY_RESERVATION + report.getReservationId();
            locker = lock.tryLock(lockKey);
            consultationReportService.create(report,transactionId);
        } finally {
            lock.unlock(locker);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ConsultationReport report, String transactionId) throws Exception {
        //这里才把业务数据入库并记录rocketmq事务日志用于回查
        baseMapper.fullData(String.valueOf(report.getId()));
        NewReservation newReservation = reservationService.getById(report.getReservationId());
        if (newReservation.getStatus() != 2) {
            reservationService.updateStatus(newReservation.getId(), 2, null, null);
        }
        //记录mq事务日志
        RocketmqTransactionLog mqLog = new RocketmqTransactionLog();
        //这个log字段自定义消息，系统不会使用
        mqLog.setLog("ConsultationReport json ： " + JsonUtils.writeValueAsString(report));
        mqLog.setTransactionId(transactionId);
        rocketmqTransactionLogMapper.insert(mqLog);
    }

    @Override
    public void update(ConsultationReportReqVO vo) {
        ConsultationReport reportModel = baseMapper.selectById(vo.getId());
        //预警数据不能修改
        vo.setIsEarlyWarning(null);
        vo.setConsultResult(null);
        vo.setWarningLevel(null);
        vo.setReferralDescription(null);
        BeanUtil.copyProperties(vo, reportModel, CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
        baseMapper.updateById(reportModel);
    }

    @Override
    public ConsultationReportRespVO get(Long reservationId) {
        ConsultationReportRespVO result = new ConsultationReportRespVO();
        ConsultationReport reportModel =
            baseMapper.selectOne(new QueryWrapper<ConsultationReport>().eq("reservation_id", reservationId));
        if (reportModel == null)
            return new ConsultationReportRespVO();
        BeanUtil.copyProperties(reportModel, result);
        result.setStartTime(reportModel.getStartTime());
        result.setEndTime(reportModel.getEndTime());
        return result;
    }

    private void checkIsEarlyWarning(ConsultationReportReqVO vo) {
        Integer isEarlyWarning = vo.getIsEarlyWarning();
        if (isEarlyWarning != null) {
            if (isEarlyWarning == 1) {
                Assert.notNull(vo.getWarningLevel(), "关注等级不能为空");
                Assert.notNull(vo.getConsultResult(), "干预结果不能为空");
            }
        }
    }

    @Override
    public WarningRecordFeignDTO addWarningRecord(NewReservation newReservation,ConsultationReport report) {
        Integer isEarlyWarning = report.getIsEarlyWarning();
        if (isEarlyWarning == null || isEarlyWarning == 0) {
            return null;
        }
        WarningRecordFeignDTO dto = new WarningRecordFeignDTO();
        dto.setUserId(newReservation.getUserId());
        dto.setUserRoleType(newReservation.getUserRoleType());
        dto.setSourceType(WarningSourceEnum.CONSULT.getCode());
        dto.setSourceRelId(report.getReservationId());
        //干预结果 0 跟踪观察 1 解除关注 2 需要转介
        if (ConsultResultEnum.HANDLED_FOLLOW.getCode().equals(report.getConsultResult())) {
            dto.setWarningStatus(WarningStatus.HANDLED_FOLLOW.getCode());
        } else if (ConsultResultEnum.UNABLE_HANDLED.getCode().equals(report.getConsultResult())) {
            dto.setWarningStatus(WarningStatus.UNABLE_HANDLED.getCode());
        } else {
            throw new IllegalArgumentException("预警干预结果状态有误");
        }
        //关注等级 0 一般关注 1 中度关注 2 高度关注
        Integer warningLevel = report.getWarningLevel();
        if (ConsultWarningLevelEnum.LOW_WARNING.getCode().equals(warningLevel)) {
            dto.setWarningGrade(WarningLevelEnum.LOW_WARNING.getCode());
        } else if (ConsultWarningLevelEnum.MID_WARNING.getCode().equals(warningLevel)) {
            dto.setWarningGrade(WarningLevelEnum.MID_WARNING.getCode());
        } else {
            dto.setWarningGrade(WarningLevelEnum.HIGH_WARNING.getCode());
        }
        dto.setReferralDescription(report.getReferralDescription());
        dto.setCreateBy(newReservation.getPsychiatristId());
        dto.setCreateTime(new Date());
        //通过mq发送，不再通过feign调用
//        warningFeign.addWarningRecord(dto);
        return dto;
    }

}
