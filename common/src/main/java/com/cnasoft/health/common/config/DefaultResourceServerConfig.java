package com.cnasoft.health.common.config;

import com.cnasoft.health.common.properties.SecurityProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.expression.OAuth2WebSecurityExpressionHandler;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.annotation.Resource;

/**
 * 资源服务器配置
 *
 * @author cnasoft
 * @date 2020/6/30 19:09
 */
@Import({SecurityHandlerConfig.class, PasswordConfig.class})
public class DefaultResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Resource
    private TokenStore tokenStore;


    @Autowired
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private OAuth2WebSecurityExpressionHandler expressionHandler;

    @Autowired
    private OAuth2AccessDeniedHandler accessDeniedHandler;

    @Autowired
    private SecurityProperties securityProperties;

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.stateless(true).tokenStore(tokenStore).authenticationEntryPoint(authenticationEntryPoint).expressionHandler(expressionHandler).accessDeniedHandler(accessDeniedHandler);
    }


    /**
     * anyRequest          |   匹配所有请求路径
     * access              |   SpringEl表达式结果为true时可以访问
     * anonymous           |   匿名可以访问
     * denyAll             |   用户不能访问
     * fullyAuthenticated  |   用户完全认证可以访问（非remember-me下自动登录）
     * hasAnyAuthority     |   如果有参数，参数表示权限，则其中任何一个权限可以访问
     * hasAnyRole          |   如果有参数，参数表示角色，则其中任何一个角色可以访问
     * hasAuthority        |   如果有参数，参数表示权限，则其权限可以访问
     * hasIpAddress        |   如果有参数，参数表示IP地址，如果用户IP和参数匹配，则可以访问
     * hasRole             |   如果有参数，参数表示角色，则其角色可以访问
     * permitAll           |   用户可以任意访问
     * rememberMe          |   允许通过remember-me登录的用户访问
     * authenticated       |   用户登录后可访问
     *
     * @param http
     * @throws Exception
     */

    @Override
    public void configure(HttpSecurity http) throws Exception {
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.AuthorizedUrl authorizedUrl = setHttp(http).authorizeRequests().antMatchers(securityProperties.getIgnore().getUrls()).permitAll().antMatchers(HttpMethod.OPTIONS).permitAll().anyRequest();
        setAuthenticate(authorizedUrl);

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).and().httpBasic().disable().headers().frameOptions().disable().and().csrf().disable();
    }

    /**
     * url权限控制，默认是认证就通过，可以重写实现个性化
     *
     * @param authorizedUrl
     */
    public HttpSecurity setAuthenticate(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.AuthorizedUrl authorizedUrl) {
        return authorizedUrl.authenticated().and();
    }

    /**
     * 留给子类重写扩展功能
     *
     * @param http
     */
    public HttpSecurity setHttp(HttpSecurity http) {
        return http;
    }

}
