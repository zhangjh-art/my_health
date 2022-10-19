package com.cnasoft.health.userservice.service.impl.approve.handler;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import com.cnasoft.health.common.annotation.approve.ApproveBeanName;
import com.cnasoft.health.common.constant.RocketMQConstant;
import com.cnasoft.health.common.dto.GaugeInfoUpdateReqVO;
import com.cnasoft.health.common.dto.TransactionMsgDefinationDTO;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.util.JsonUtils;
import com.cnasoft.health.common.util.SysUserUtil;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.feign.TaskFeign;
import com.cnasoft.health.userservice.mapper.RocketmqTransactionLogMapper;
import com.cnasoft.health.userservice.model.Approve;
import com.cnasoft.health.userservice.model.RocketmqTransactionLog;
import com.cnasoft.health.userservice.service.IApproveService;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.cnasoft.health.common.exception.constant.GlobalErrorCodeConstants.BAD_REQUEST;
import static com.cnasoft.health.common.exception.util.ServiceExceptionUtil.exception;

/**
 * @author Administrator
 */
@Service(ApproveBeanName.APPROVE_GAUGE_HANDLER)
public class ApproveGaugeHandleServiceImpl implements ApproveHandleService {
    @Resource
    private TaskFeign taskFeign;
    @Resource
    RocketMQTemplate rocketMQTemplate;
    @Resource
    RocketmqTransactionLogMapper rocketmqTransactionLogMapper;
    @Resource
    private IApproveService approveService;

    @Override
    public void handleAddApprove(Approve approve, boolean allow) throws Exception {
        int approveStatus = allow ? ApproveStatus.APPROVED.getCode() : ApproveStatus.REJECTED.getCode();
        HashMap<String, Object> params = new HashMap<>();
        params.put("businessId", approve.getBusinessId());
        params.put("approveStatus", approveStatus);
        params.put("approveRemark", approve.getApproveRemark());
        String transactionId = UUID.randomUUID().toString();
        TransactionMsgDefinationDTO msgDefinationDTO = new TransactionMsgDefinationDTO();
        msgDefinationDTO.setDestClass(this.getClass());
        msgDefinationDTO.setMethod("doHandleApprove");
        msgDefinationDTO.setArg(approve);
        //三个业务参数  dto-->消费端用参数    userDynamic-->当前业务参数  transactionId-->事务消息唯一编码
        TransactionSendResult sendResult = rocketMQTemplate.sendMessageInTransaction(Constant.ADD_WARNING_GROUP, RocketMQConstant.HANDLE_ADD_APPROVE_TOPIC,
                MessageBuilder.withPayload(params)
                        .setHeader(RocketMQHeaders.TRANSACTION_ID, transactionId)
                        .build()
                , msgDefinationDTO
        );
        if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
            //mq 异常
            throw exception("系统繁忙,请稍后重试");
        }
        if (LocalTransactionState.ROLLBACK_MESSAGE.equals(sendResult.getLocalTransactionState())) {
            //数据库异常
            throw exception("系统繁忙,请稍后重试");
        }
    }

    @Override
    @Transactional
    public void handleDeleteApprove(Approve approve) {
        String transactionId = UUID.randomUUID().toString();
        TransactionMsgDefinationDTO msgDefinationDTO = new TransactionMsgDefinationDTO();
        msgDefinationDTO.setDestClass(this.getClass());
        msgDefinationDTO.setMethod("doHandleApprove");
        msgDefinationDTO.setArg(approve);
        //三个业务参数  dto-->消费端用参数    userDynamic-->当前业务参数  transactionId-->事务消息唯一编码
        TransactionSendResult sendResult = rocketMQTemplate.sendMessageInTransaction(Constant.ADD_WARNING_GROUP, RocketMQConstant.HANDLE_DELETE_APPROVE_TOPIC,
                MessageBuilder.withPayload(Collections.singleton(approve.getBusinessId()))
                        .setHeader(RocketMQHeaders.TRANSACTION_ID, transactionId)
                        .build()
                , msgDefinationDTO
        );
        if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
            //mq 异常
            throw exception("系统繁忙,请稍后重试");
        }
        if (LocalTransactionState.ROLLBACK_MESSAGE.equals(sendResult.getLocalTransactionState())) {
            //数据库异常
            throw exception("系统繁忙,请稍后重试");
        }
    }

    @Override
    public void handleUpdateApprove(Approve approve) throws Exception {
        GaugeInfoUpdateReqVO vo = JsonUtils.readValue(approve.getAfterString(), GaugeInfoUpdateReqVO.class);
        assert vo != null;
        Assert.notNull(vo, BAD_REQUEST.getMessage());

        String transactionId = UUID.randomUUID().toString();
        TransactionMsgDefinationDTO msgDefinationDTO = new TransactionMsgDefinationDTO();
        msgDefinationDTO.setDestClass(this.getClass());
        msgDefinationDTO.setMethod("doHandleApprove");
        msgDefinationDTO.setArg(approve);
        //三个业务参数  dto-->消费端用参数    userDynamic-->当前业务参数  transactionId-->事务消息唯一编码
        TransactionSendResult sendResult = rocketMQTemplate.sendMessageInTransaction(Constant.ADD_WARNING_GROUP, RocketMQConstant.HANDLE_UPDATE_APPROVE_TOPIC,
                MessageBuilder.withPayload(vo)
                        .setHeader(RocketMQHeaders.TRANSACTION_ID, transactionId)
                        .build()
                , msgDefinationDTO
        );
        if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
            //mq 异常
            throw exception("系统繁忙,请稍后重试");
        }
        if (LocalTransactionState.ROLLBACK_MESSAGE.equals(sendResult.getLocalTransactionState())) {
            //数据库异常
            throw exception("系统繁忙,请稍后重试");
        }
    }

    @Override
    public void handleEnableApprove(Approve approve) {
        Map<String, Object> map = JsonUtils.readValue(approve.getAfterString(), HashMap.class);

        String transactionId = UUID.randomUUID().toString();
        TransactionMsgDefinationDTO msgDefinationDTO = new TransactionMsgDefinationDTO();
        msgDefinationDTO.setDestClass(this.getClass());
        msgDefinationDTO.setMethod("doHandleApprove");
        msgDefinationDTO.setArg(approve);
        //三个业务参数  dto-->消费端用参数    userDynamic-->当前业务参数  transactionId-->事务消息唯一编码
        TransactionSendResult sendResult = rocketMQTemplate.sendMessageInTransaction(Constant.ADD_WARNING_GROUP, RocketMQConstant.HANDLE_ENABLE_APPROVE_TOPIC,
                MessageBuilder.withPayload(map)
                        .setHeader(RocketMQHeaders.TRANSACTION_ID, transactionId)
                        .build()
                , msgDefinationDTO
        );
        if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
            //mq 异常
            throw exception("系统繁忙,请稍后重试");
        }
        if (LocalTransactionState.ROLLBACK_MESSAGE.equals(sendResult.getLocalTransactionState())) {
            //数据库异常
            throw exception("系统繁忙,请稍后重试");
        }
    }

    @Override
    public List<Long> queryApproveBusinessId(Map<String, Object> params) {
        String query = MapUtil.getStr(params, "query");
        String gaugeType = MapUtil.getStr(params, "gaugeType");
        if (StringUtils.isEmpty(query) && StringUtils.isEmpty(gaugeType)) {
            return null;
        }
        List<Long> result = taskFeign.getGaugeIdByQuery(query, gaugeType, null).getData();
        return result == null ? new ArrayList<>() : result;
    }

    @Transactional
    public void doHandleApprove(Approve approve, String transactionId) throws Exception {
        approve.setApproveTime(new Date());
        approve.setApproveUserId(SysUserUtil.getHeaderUserId());
        approveService.saveOrUpdate(approve);
        //记录mq事务日志
        RocketmqTransactionLog mqLog = new RocketmqTransactionLog();
        //这个log字段自定义消息，系统不会使用
        mqLog.setLog("approve id ： " + approve.getId() + ", status: " + approve.getApproveStatus() + ", remark: " + approve.getApproveRemark());
        mqLog.setTransactionId(transactionId);
        rocketmqTransactionLogMapper.insert(mqLog);
    }
}
