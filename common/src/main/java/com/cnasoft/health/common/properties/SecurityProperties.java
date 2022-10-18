package com.cnasoft.health.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * Spring Security 入口配置
 *
 * @author cnasoft
 * @date 2020/6/30 18:28
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "cnasoft.security")
@RefreshScope
public class SecurityProperties {

    private AuthProperties auth = new AuthProperties();

    private PermitProperties ignore = new PermitProperties();

    private ValidateCodeProperties code = new ValidateCodeProperties();
}