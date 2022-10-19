package com.cnasoft.health.auth.pad;

import com.cnasoft.health.auth.service.CnaSoftUserDetailService;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * PAD学生扫码登录-配置文件
 *
 * @author ganghe
 * @date 2022/7/19 19:51
 */
@Component
public class PadAuthenticationSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {
    @Resource
    private CnaSoftUserDetailService userDetailsService;

    @Override
    public void configure(HttpSecurity http) {
        PadAuthenticationProvider provider = new PadAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        http.authenticationProvider(provider);
    }
}
