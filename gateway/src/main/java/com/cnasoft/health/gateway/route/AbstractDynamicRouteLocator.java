package com.cnasoft.health.gateway.route;

import org.springframework.cloud.netflix.zuul.filters.RefreshableRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.SimpleRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 动态路由实现
 */
public abstract class AbstractDynamicRouteLocator extends SimpleRouteLocator implements RefreshableRouteLocator {
    private ZuulProperties zuulProperties;

    public AbstractDynamicRouteLocator(String servletPath, ZuulProperties properties) {
        super(servletPath, properties);
        this.zuulProperties = properties;
    }

    @Override
    public void refresh() {
        doRefresh();
    }

    /**
     * 重写路由定位实现
     * 1. 先从配置文件中加载静态路由信息  --> 2. 从动态数据源中加载动态路由信息
     *
     * @return
     * @see SimpleRouteLocator#locateRoutes()
     */
    @Override
    public Map<String, ZuulRoute> locateRoutes() {
        LinkedHashMap<String, ZuulRoute> routesMap = new LinkedHashMap<>();
        routesMap.putAll(super.locateRoutes());
        routesMap.putAll(loadDynamicRoute());
        LinkedHashMap<String, ZuulRoute> values = new LinkedHashMap<>();
        for (Map.Entry<String, ZuulRoute> entry : routesMap.entrySet()) {
            String path = entry.getKey();
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            if (StringUtils.hasText(zuulProperties.getPrefix())) {
                path = zuulProperties.getPrefix() + path;
                if (!path.startsWith("/")) {
                    path = "/" + path;
                }
            }
            values.put(path, entry.getValue());
        }
        return values;
    }

    /**
     * 从动态数据源加载路由配置抽象方法定义
     */
    public abstract Map<String, ZuulRoute> loadDynamicRoute();

}
