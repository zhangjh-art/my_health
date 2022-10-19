package com.cnasoft.health.userservice.service.impl.mq.listener;

import com.cnasoft.health.common.dto.TransactionMsgDefinationDTO;
import com.cnasoft.health.common.util.reflect.ReflectionUtil;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.service.impl.mq.CheckLocalTransaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import javax.annotation.Resource;
import java.lang.reflect.Method;

@Slf4j
@RocketMQTransactionListener(txProducerGroup = Constant.ADD_WARNING_GROUP)
public class AddWarningListener implements RocketMQLocalTransactionListener {
    @Resource
    CheckLocalTransaction checkLocalTransaction;

    @Resource
    ApplicationContext applicationContext;

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object arg) {
        MessageHeaders headers = message.getHeaders();
        //获取事务ID
        String transactionId = (String) headers.get(RocketMQHeaders.TRANSACTION_ID);
        try {
            //执行本地事务，并记录日志
            TransactionMsgDefinationDTO dto = (TransactionMsgDefinationDTO)arg;
            Object bean = applicationContext.getBean(dto.getDestClass());
            Method method = ReflectionUtil.getMethod(dto.getDestClass(), dto.getMethod(),  dto.getArg().getClass(), String.class);
            method.invoke(bean, dto.getArg(), transactionId);
            //执行成功，可以提交事务
            return RocketMQLocalTransactionState.COMMIT;
        } catch (Exception e) {
            log.error("事务消息保存用户动态异常：+" + e.getMessage(), e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        return checkLocalTransaction.checkLocalTransaction(message);
    }
}
