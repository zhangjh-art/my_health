package com.cnasoft.health.common.properties;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cnasoft
 * @date 2020/6/30 18:29
 */
@Setter
@Getter
public class PermitProperties {

    /**
     * 默认不校验
     */
    private static final String[] ENDPOINTS = {
            "/oauth/**",
            "/actuator/**",
            "/*/v1/api-docs",
            "/swagger/api-docs",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/druid/**"
    };


    private String[] httpUrls = {};

    /**
     * 聚合默认和用户自定义url
     *
     * @return
     */
    public String[] getUrls() {
        if (httpUrls == null || httpUrls.length == 0) {
            return ENDPOINTS;
        }
        List<String> list = new ArrayList<>();
        for (String url : ENDPOINTS) {
            list.add(url);
        }
        for (String url : httpUrls) {
            list.add(url);
        }
        return list.toArray(new String[list.size()]);
    }
}
