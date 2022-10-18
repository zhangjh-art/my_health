package com.cnasoft.health.gateway.config;

import com.cnasoft.health.gateway.interceptor.PrometheusMetricsInterceptor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.cloud.netflix.zuul.web.ZuulHandlerMapping;
import org.springframework.context.annotation.Configuration;

/**
 * @function 由于zuul是走的自己的zuul servlet,不像mvc servlet注册了interceptor, 这里通过spring bean生命周期手动注入
 */
@Configuration
@RequiredArgsConstructor
public class InterceptorConfig extends InstantiationAwareBeanPostProcessorAdapter {

    @NonNull
    private final PrometheusMetricsInterceptor prometheusMetricsInterceptor;

    @Override
    public boolean postProcessAfterInstantiation(final Object bean, final String beanName) throws BeansException {
        if (bean instanceof ZuulHandlerMapping) {
            val zuulHandlerMapping = (ZuulHandlerMapping) bean;
            zuulHandlerMapping.setInterceptors(prometheusMetricsInterceptor);
        }
        return super.postProcessAfterInstantiation(bean, beanName);
    }

}
