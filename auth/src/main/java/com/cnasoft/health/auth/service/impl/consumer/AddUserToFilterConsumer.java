package com.cnasoft.health.auth.service.impl.consumer;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cnasoft.health.auth.mapper.MQMessageConsumedMapper;
import com.cnasoft.health.auth.model.MQMessageConsumed;
import com.cnasoft.health.auth.service.CnaSoftUserDetailService;
import com.cnasoft.health.common.constant.RocketMQConstant;
import com.cnasoft.health.common.lock.IDistLock;
import com.cnasoft.health.common.lock.LockKeyConstant;
import com.cnasoft.health.common.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Slf4j
@Service
@RocketMQMessageListener(topic = RocketMQConstant.ADD_USER_FILTER_TOPIC, consumerGroup = RocketMQConstant.ADD_USER_FILTER_GROUP)
public class AddUserToFilterConsumer implements RocketMQListener<MessageExt> {

    @Resource
    MQMessageConsumedMapper mqMessageConsumedMapper;
    @Resource
    private CnaSoftUserDetailService userDetailsService;
    @Resource
    private IDistLock lock;

    @Override
    public void onMessage(MessageExt messageExt) {
        String uniqKey = messageExt.getProperty("UNIQ_KEY");
        log.info("message id " + uniqKey + ", consume times " + messageExt.getReconsumeTimes());
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
            Set<String> usernameAndMobileList = JsonUtils.readValue(json, new TypeReference<Set<String>>() {});
            userDetailsService.addUserToFilter(usernameAndMobileList);
            // 保存messageId，防止消息重复消费
            MQMessageConsumed consumed = new MQMessageConsumed();
            consumed.setMessageId(uniqKey);
            mqMessageConsumedMapper.insert(consumed);
        } catch (Exception e) {
            //异常时1、重试3次 2、写日志 后续可以增加发送通知人工处理
            if (messageExt.getReconsumeTimes() > 3) {
                log.info("【消息队列】消费add-user-filter异常" + JsonUtils.writeValueAsString(messageExt));
                // 保存messageId，防止消息重复消费
                MQMessageConsumed consumed = new MQMessageConsumed();
                consumed.setMessageId(uniqKey);
                mqMessageConsumedMapper.insert(consumed);
                return;
            }

            //只要抛出异常mq就会重试消费，默认是重试16次
            throw new RuntimeException("消息队列消费add-user-filter异常" + e.getMessage());
        }finally {
            try {
                lock.unlock(locker);
            } catch (Exception e) {
                log.error("释放分布式锁失败");
            }
        }
    }
}

