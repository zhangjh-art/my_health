package com.cnasoft.health.userservice.constant;

import com.cnasoft.health.common.constant.RedisConstant;

/**
 * @author cnasoft
 * @date 2020/7/4 10:07
 */
public interface RedisDataSource {
    String USERSENTINEL_SOURCE = "usersentinel" + RedisConstant.REDIS_REPOSITORY_BEAN_NAME_SUFFIX;
}
