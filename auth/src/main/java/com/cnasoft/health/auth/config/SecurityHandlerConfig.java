package com.cnasoft.health.auth.config;

import com.cnasoft.health.auth.handler.OauthLogoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidScopeException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.common.exceptions.RedirectMismatchException;
import org.springframework.security.oauth2.common.exceptions.UnsupportedResponseTypeException;
import org.springframework.security.oauth2.provider.error.DefaultWebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author cnasoft
 * @date 2020/6/3 08:34
 */
@Slf4j
@Configuration
public class SecurityHandlerConfig {
    /**
     * 退出登录
     *
     * @return
     */
    @Bean
    public OauthLogoutHandler oauthLogoutHandler() {
        return new OauthLogoutHandler();
    }

    /**
     * 个性化异常转换器
     *
     * @return
     */
    @Bean
    public WebResponseExceptionTranslator webResponseExceptionTranslator() {
        return new DefaultWebResponseExceptionTranslator() {
            public static final String BAD_MSG = "坏的凭证";

            @Override
            public ResponseEntity<OAuth2Exception> translate(Exception e) throws Exception {
                OAuth2Exception oAuth2Exception;
                if (e.getMessage() != null && e.getMessage().equals(BAD_MSG)) {
                    oAuth2Exception = new InvalidGrantException("用户名或密码错误", e);
                } else if (e instanceof InternalAuthenticationServiceException) {
                    oAuth2Exception = new InvalidGrantException(e.getMessage(), e);
                } else if (e instanceof RedirectMismatchException) {
                    oAuth2Exception = new InvalidGrantException(e.getMessage(), e);
                } else if (e instanceof InvalidScopeException) {
                    oAuth2Exception = new InvalidGrantException(e.getMessage(), e);
                } else {
                    oAuth2Exception = new UnsupportedResponseTypeException("服务内部错误", e);
                }
                ResponseEntity<OAuth2Exception> response = super.translate(oAuth2Exception);
                ResponseEntity.status(oAuth2Exception.getHttpErrorCode());
                response.getBody().addAdditionalInformation("code", oAuth2Exception.getHttpErrorCode() + "");
                response.getBody().addAdditionalInformation("msg", oAuth2Exception.getMessage());

                return response;
            }
        };
    }

    /**
     * 登录成功
     * @return
     */
    @Bean
    public AuthenticationSuccessHandler loginSuccessHandler() {
        return new SavedRequestAwareAuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                                Authentication authentication) throws IOException, ServletException {
                super.onAuthenticationSuccess(request, response, authentication);
            }
        };
    }
}
