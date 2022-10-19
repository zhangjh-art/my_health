package com.cnasoft.health.userservice.service.impl.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cnasoft.health.userservice.mapper.RocketmqTransactionLogMapper;
import com.cnasoft.health.userservice.model.RocketmqTransactionLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class CheckLocalTransaction {
    @Resource
    RocketmqTransactionLogMapper rocketmqTransactionLogMapper;

    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        MessageHeaders headers = message.getHeaders();
        //获取事务ID
        String transactionId = (String) headers.get(RocketMQHeaders.TRANSACTION_ID);
        //通过transactionId 查询事务日志
        LambdaQueryWrapper<RocketmqTransactionLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RocketmqTransactionLog::getTransactionId,transactionId);
        RocketmqTransactionLog entity = rocketmqTransactionLogMapper.selectOne(wrapper);
        if (entity == null) {
            return RocketMQLocalTransactionState.ROLLBACK;
        }
        return RocketMQLocalTransactionState.COMMIT;
    }
}
