package com.cnasoft.health.auth.constant;

import com.cnasoft.health.common.constant.RedisConstant;

/**
 * @author cnasoft
 * @date 2020/7/4 10:07
 */
public interface RedisDataSource {
    String AUTH_TOKEN_SOURCE = "auth-token-sentinel" + RedisConstant.REDIS_REPOSITORY_BEAN_NAME_SUFFIX;
    String AUTH_REDIS_CONNECTION_FACTORY = "auth-token-sentinel" + RedisConstant.REDIS_CONNECTION_FACTORY_BEAN_NAME_SUFFIX;
    String SLIDER_CAPTCHA_ACCOUNT = "slider_captcha_account:";
}
