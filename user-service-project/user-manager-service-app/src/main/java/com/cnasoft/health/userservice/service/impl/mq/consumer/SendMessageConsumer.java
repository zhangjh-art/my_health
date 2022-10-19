package com.cnasoft.health.userservice.service.impl.mq.consumer;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cnasoft.health.common.constant.RocketMQConstant;
import com.cnasoft.health.common.dto.MessageDTO;
import com.cnasoft.health.common.lock.IDistLock;
import com.cnasoft.health.common.lock.LockKeyConstant;
import com.cnasoft.health.common.util.JsonUtils;
import com.cnasoft.health.userservice.mapper.MQMessageConsumedMapper;
import com.cnasoft.health.userservice.model.MQMessageConsumed;
import com.cnasoft.health.userservice.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RocketMQMessageListener(topic = RocketMQConstant.SEND_MESSAGE_TOPIC, consumerGroup = RocketMQConstant.SEND_MESSAGE_GROUP)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendMessageConsumer implements RocketMQListener<MessageExt> {

    @Resource
    private ISysUserService sysUserService;
    @Resource
    private IDistLock lock;
    @Resource
    MQMessageConsumedMapper mqMessageConsumedMapper;

    @Override
    public void onMessage(MessageExt messageExt) {
        String uniqKey = messageExt.getProperty("UNIQ_KEY");
        Object locker = null;

        try {
            String lockKey = LockKeyConstant.WARMING_KEY + uniqKey;
            locker = lock.tryLock(lockKey);
            // 防止消息重复消费
            MQMessageConsumed messageConsumed = mqMessageConsumedMapper.selectOne(new QueryWrapper<MQMessageConsumed>().eq("message_id", uniqKey));
            if (messageConsumed != null) {
                log.info("消息已经被消费过了，不再重复消费，message id：" + uniqKey);
                return;
            }
            //消费消息入库，完成整个业务。这里可能出错，出错后，前一个服务的业务不会回滚，只能人工处理
            String json = new String(messageExt.getBody(), StandardCharsets.UTF_8);
            MessageDTO dto = JsonUtils.readValue(json, MessageDTO.class);
            sysUserService.sendMessage(dto);
            // 保存messageId，防止消息重复消费
            MQMessageConsumed consumed = new MQMessageConsumed();
            consumed.setMessageId(uniqKey);
            mqMessageConsumedMapper.insert(consumed);
        } catch (Exception e) {
            //异常时1、重试3次 2、写日志 后续可以写数据库、增加发送通知人工处理
            if (messageExt.getReconsumeTimes() > 3) {
                log.info("【消息队列】消费send-message-topic异常" + JsonUtils.writeValueAsString(messageExt));
                MQMessageConsumed consumed = new MQMessageConsumed();
                consumed.setMessageId(uniqKey);
                mqMessageConsumedMapper.insert(consumed);
                return;
            }
            //只要抛出异常mq就会重试消费，默认是重试16次
            throw new RuntimeException("消息队列消费send-message-topic异常" + e.getMessage());
        }finally {
            try {
                lock.unlock(locker);
            } catch (Exception e) {
                log.error("释放分布式锁失败");
            }
        }
    }
}
