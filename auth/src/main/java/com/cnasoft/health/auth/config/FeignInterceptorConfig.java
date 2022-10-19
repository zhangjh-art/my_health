package com.cnasoft.health.auth.config;

import com.cnasoft.health.common.constant.CommonConstant;
import com.cnasoft.health.common.constant.SecurityConstants;
import com.cnasoft.health.common.context.TenantContextHolder;
import feign.RequestInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;

/**
 * @author cnasoft
 * @date 2020/6/8 10:41
 */
public class FeignInterceptorConfig {

    /**
     * 使用feign client访问别的微服务时，将上游传过来的clientId、traceId等信息放入header传递给下一个微服务
     */
    @Bean
    public RequestInterceptor baseFeignInterceptor() {
        return template -> {
            //传递clientId
            String tenant = TenantContextHolder.getTenant();
            if (StringUtils.isNotEmpty(tenant)) {
                template.header(SecurityConstants.TENANT_HEADER, tenant);
            }

            //传递日志traceId
            String traceId = MDC.get(CommonConstant.LOG_TRACE_ID);
            if (StringUtils.isNotEmpty(traceId)) {
                template.header(CommonConstant.TRACE_ID_HEADER, traceId);
            }
        };
    }
}
