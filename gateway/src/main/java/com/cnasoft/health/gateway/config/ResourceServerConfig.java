package com.cnasoft.health.gateway.config;

import com.cnasoft.health.common.config.DefaultResourceServerConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

/**
 * 网关资源服务器配置
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends DefaultResourceServerConfig {

    @Override
    public HttpSecurity setAuthenticate(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.AuthorizedUrl authorizedUrl) {
        return authorizedUrl.access("@permissionService.hasPermission(request, authentication)").and();
    }
}
