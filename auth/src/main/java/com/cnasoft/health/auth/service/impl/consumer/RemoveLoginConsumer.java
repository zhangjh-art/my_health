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
            // ????????????????????????
            MQMessageConsumed messageConsumed = mqMessageConsumedMapper.selectOne(new QueryWrapper<MQMessageConsumed>().eq("message_id", uniqKey));
            if (messageConsumed != null) {
                log.info("???????????????????????????????????????????????????message id???" + uniqKey);
                return;
            }
            //????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
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

            // ??????messageId???????????????????????????
            MQMessageConsumed consumed = new MQMessageConsumed();
            consumed.setMessageId(uniqKey);
            mqMessageConsumedMapper.insert(consumed);
        } catch (Exception e) {
            //?????????1?????????3??? 2???????????? ??????????????????????????????????????????
            if (messageExt.getReconsumeTimes() > 3) {
                log.info("????????????????????????add-user-filter??????" + JsonUtils.writeValueAsString(messageExt));
                // ??????messageId???????????????????????????
                MQMessageConsumed consumed = new MQMessageConsumed();
                consumed.setMessageId(uniqKey);
                mqMessageConsumedMapper.insert(consumed);
                return;
            }

            //??????????????????mq????????????????????????????????????16???
            throw new RuntimeException("??????????????????add-user-filter??????" + e.getMessage());
        } finally {
            try {
                lock.unlock(locker);
            } catch (Exception e) {
                log.error("????????????????????????");
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
