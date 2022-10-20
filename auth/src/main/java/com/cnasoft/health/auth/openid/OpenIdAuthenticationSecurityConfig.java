package com.cnasoft.health.auth.openid;

import com.cnasoft.health.auth.service.CnaSoftUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.stereotype.Component;

/**
 * @author cnasoft
 * @date 2020/7/2 19:51
 */
@Component
public class OpenIdAuthenticationSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {
    @Autowired
    private CnaSoftUserDetailService userDetailsService;

    @Override
    public void configure(HttpSecurity http) {
        OpenIdAuthenticationProvider provider = new OpenIdAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        http.authenticationProvider(provider);
    }
}
