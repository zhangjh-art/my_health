package com.cnasoft.health.auth.username;

import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author cnasoft
 * @date 2020/7/2 19:51
 */
@Component
public class CustomUsernamePasswordAuthenticationSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {
    @Resource
    private UserDetailsService userDetailsService;

    @Override
    public void configure(HttpSecurity http) {
        CustomUsernamePasswordAuthenticationProvider provider = new CustomUsernamePasswordAuthenticationProvider();
        provider.setUserDetailService(userDetailsService);
        http.authenticationProvider(provider);
    }
}
