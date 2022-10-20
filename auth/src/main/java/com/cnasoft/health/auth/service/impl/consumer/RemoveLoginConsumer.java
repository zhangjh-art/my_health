package com.cnasoft.health.auth.service.impl.consumer;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.cnasoft.health.auth.mapper.MQMessageConsumedMapper;
import com.cnasoft.health.auth.model.MQMessageConsumed;
import com.cnasoft.health.common.constant.RocketMQConstant;
import com.cnasoft.health.common.dto.SysUserDTO;
import com.cnasoft.health.common.enums.RoleEnum;
import com.cnasoft.health.common.lock.IDistLock;
import com.cnasoft.health.common.lock.LockKeyConstant;
import com.cnasoft.health.common.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
@RocketMQMessageListener(topic = RocketMQConstant.REMOVE_LOGIN_TOPIC, consumerGroup = RocketMQConstant.REMOVE_LOGIN_GROUP)
public class RemoveLoginConsumer implements RocketMQListener<MessageExt> {

    @Resource
    MQMessageConsumedMapper mqMessageConsumedMapper;
    @Resource
    private TokenStore tokenStore;
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
            Set<SysUserDTO> users = JsonUtils.readValue(json, new TypeReference<Set<SysUserDTO>>() {
            });
            if (CollectionUtils.isNotEmpty(users)) {
                for (SysUserDTO user : users) {
                    getTokenAndRemoveLogin(user.getId(), user.getUsername());
                    getTokenAndRemoveLogin(user.getId(), user.getMobile());
                    if (RoleEnum.student.getValue().equals(user.getPresetRoleCode())) {
                        if (StringUtils.isNotBlank(user.getShortId())) {
                            getTokenAndRemoveLogin(user.getId(), user.getShortId());
                        }
                    }
                }
            }

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
        } finally {
            try {
                lock.unlock(locker);
            } catch (Exception e) {
                log.error("释放分布式锁失败");
            }
        }
    }

    private void getTokenAndRemoveLogin(Long userId, String username) {
        if (Objects.isNull(userId) || userId == 0L || StringUtils.isBlank(username)) {
            return;
        }

        Collection<OAuth2AccessToken> tokens = tokenStore.findTokensByClientIdAndUserName("app", username);
        for (OAuth2AccessToken accessToken : tokens) {
            OAuth2Authentication oAuth2Authentication = tokenStore.readAuthentication(accessToken);
            Object principal = oAuth2Authentication.getUserAuthentication().getPrincipal();
            if (principal instanceof SysUserDTO) {
                SysUserDTO userInfo = (SysUserDTO) principal;
                if (userInfo.getId().equals(userId)) {
                    OAuth2RefreshToken refreshToken = accessToken.getRefreshToken();
                    if (null != refreshToken) {
                        tokenStore.removeRefreshToken(refreshToken);
                    }
                    tokenStore.removeAccessToken(accessToken);
                }
            }
        }
    }
}
