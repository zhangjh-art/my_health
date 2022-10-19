package com.cnasoft.health.auth.config;

import com.cnasoft.health.auth.filter.LoginProcessSetTenantFilter;
import com.cnasoft.health.auth.h5.H5MobileAuthenticationSecurityConfig;
import com.cnasoft.health.auth.h5.H5UsernamePasswordAuthenticationSecurityConfig;
import com.cnasoft.health.auth.handler.OauthLogoutSuccessHandler;
import com.cnasoft.health.auth.mobile.MobileAuthenticationSecurityConfig;
import com.cnasoft.health.auth.pad.PadAuthenticationSecurityConfig;
import com.cnasoft.health.auth.pad.PadUsernamePasswordAuthenticationSecurityConfig;
import com.cnasoft.health.auth.tenant.TenantAuthenticationSecurityConfig;
import com.cnasoft.health.auth.tenant.TenantUsernamePasswordAuthenticationFilter;
import com.cnasoft.health.auth.username.CustomUsernamePasswordAuthenticationSecurityConfig;
import com.cnasoft.health.common.config.PasswordConfig;
import com.cnasoft.health.common.constant.SecurityConstants;
import com.cnasoft.health.common.properties.TenantProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import javax.annotation.Resource;

/**
 * @author cnasoft
 * @date 2020/6/2 21:46
 */
@Configuration
@Import(PasswordConfig.class)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthenticationSuccessHandler authenticationSuccessHandler;

    @Autowired(required = false)
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Resource
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Resource
    private LogoutHandler oauthLogoutHandler;

    @Autowired
    private MobileAuthenticationSecurityConfig mobileAuthenticationSecurityConfig;

    @Resource
    private H5MobileAuthenticationSecurityConfig h5MobileAuthenticationSecurityConfig;

    @Resource
    private H5UsernamePasswordAuthenticationSecurityConfig h5UsernamePasswordAuthenticationSecurityConfig;

    @Resource
    private PadAuthenticationSecurityConfig padAuthenticationSecurityConfig;

    @Resource
    private PadUsernamePasswordAuthenticationSecurityConfig padUsernamePasswordAuthenticationSecurityConfig;

    @Resource
    private CustomUsernamePasswordAuthenticationSecurityConfig customUsernamePasswordAuthenticationSecurityConfig;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TenantAuthenticationSecurityConfig tenantAuthenticationSecurityConfig;

    @Autowired
    private TenantProperties tenantProperties;

    /**
     * @return 认证管理对象
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public TenantUsernamePasswordAuthenticationFilter tenantAuthenticationFilter(AuthenticationManager authenticationManager) {
        TenantUsernamePasswordAuthenticationFilter filter = new TenantUsernamePasswordAuthenticationFilter();
        filter.setAuthenticationManager(authenticationManager);
        filter.setFilterProcessesUrl(SecurityConstants.OAUTH_LOGIN_PRO_URL);
        filter.setAuthenticationSuccessHandler(authenticationSuccessHandler);
        filter.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler(SecurityConstants.LOGIN_FAILURE_PAGE));
        return filter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .anyRequest()
                .permitAll()
                .and()
                .logout()
                .logoutUrl(SecurityConstants.LOGOUT_URL)
                .logoutSuccessHandler(new OauthLogoutSuccessHandler())
                .addLogoutHandler(oauthLogoutHandler)
                .clearAuthentication(true)
                .and()
                .apply(customUsernamePasswordAuthenticationSecurityConfig)
                .and()
                .apply(mobileAuthenticationSecurityConfig)
                .and()
                .apply(h5MobileAuthenticationSecurityConfig)
                .and()
                .apply(h5UsernamePasswordAuthenticationSecurityConfig)
                .and()
                .apply(padAuthenticationSecurityConfig)
                .and()
                .apply(padUsernamePasswordAuthenticationSecurityConfig)
                .and()
                .addFilterBefore(new LoginProcessSetTenantFilter(), UsernamePasswordAuthenticationFilter.class)
                .csrf().disable()
                // 解决不允许显示在iframe的问题
                .headers().frameOptions().disable().cacheControl();

        if (tenantProperties.getEnable()) {
            //解决不同租户单点登录时角色没变化
            http.formLogin()
                    .loginPage(SecurityConstants.LOGIN_PAGE)
                    .and()
                    .addFilterAt(tenantAuthenticationFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class)
                    .apply(tenantAuthenticationSecurityConfig);
        } else {
            http.formLogin()
                    .loginPage(SecurityConstants.LOGIN_PAGE)
                    .loginProcessingUrl(SecurityConstants.OAUTH_LOGIN_PRO_URL)
                    .successHandler(authenticationSuccessHandler);
        }


        // 基于密码 等模式可以无session,不支持授权码模式
        if (authenticationEntryPoint != null) {
            http.exceptionHandling().authenticationEntryPoint(authenticationEntryPoint);
            http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        } else {
            // 授权码模式单独处理，需要session的支持，此模式可以支持所有oauth2的认证
            http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
        }
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
    }

    /**
     * 全局用户信息
     */
    @Autowired
    public void globalUserDetails(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }
}
