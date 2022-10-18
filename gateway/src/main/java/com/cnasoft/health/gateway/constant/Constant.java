package com.cnasoft.health.gateway.constant;

/**
 * @author cnasoft
 * @date 2020/9/22 19:40
 */
public interface Constant {
    /**
     * 配置中心标识
     * 动态路由配置格式：
     * [
     *     {
     *         "id":"api-user",
     *         "path":"/api-user/**",
     *         "serviceId":"user-service"
     *     }
     * ]
     */
    String ZUUL_DATA_ID = "zuul-routes-toc";
    String ZUUL_GROUP_ID = "ZUUL_GATEWAY_TOC";
}
