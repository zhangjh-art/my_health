package com.cnasoft.health.userservice.service.impl.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.messaging.Message;

import javax.annotation.Resource;

@Slf4j
@RocketMQTransactionListener(txProducerGroup = "SendMessageGroup")
public class SendMessageListener implements RocketMQLocalTransactionListener {
    @Resource
    CheckLocalTransaction checkLocalTransaction;

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object arg) {
        //直接返回，由checkLocalTransaction 定时、异步，再去处理数据。 这样就是异步消息，这里数据异常，当前的强制业务也不会回滚
        return RocketMQLocalTransactionState.UNKNOWN;
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        return checkLocalTransaction.checkLocalTransaction(message);
    }
}
