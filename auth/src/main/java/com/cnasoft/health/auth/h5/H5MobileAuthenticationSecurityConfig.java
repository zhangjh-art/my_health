package com.cnasoft.health.auth.h5;

import com.cnasoft.health.auth.service.CnaSoftUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.stereotype.Component;

/**
 * @author cnasoft
 * @date 2020/7/2 19:56
 */
@Component
public class H5MobileAuthenticationSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {
    @Autowired
    private CnaSoftUserDetailService userDetailsService;

    @Override
    public void configure(HttpSecurity http) {
        //mobile provider
        H5MobileAuthenticationProvider provider = new H5MobileAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        http.authenticationProvider(provider);
    }
}
