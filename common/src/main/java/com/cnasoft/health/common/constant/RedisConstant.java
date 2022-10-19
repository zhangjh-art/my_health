package com.cnasoft.health.common.constant;

/**
 */
public interface RedisConstant {
    String REDIS_STRING_TEMPLATE_BEAN_NAME_SUFFIX = "-string-redis-template";
    String REDIS_TEMPLATE_BEAN_NAME_SUFFIX = "-redis-template";
    String REDIS_CONNECTION_FACTORY_BEAN_NAME_SUFFIX = "-redis-connection-factory";
    String REDIS_REPOSITORY_BEAN_NAME_SUFFIX = "-repository";

    /**
     * 用户缓存redis key
     */
    String USER_SUFFIX = "sys_user:";

    /**
     * 区域缓存redis key
     */
    String AREA_SUFFIX = "sys_area:";
}
