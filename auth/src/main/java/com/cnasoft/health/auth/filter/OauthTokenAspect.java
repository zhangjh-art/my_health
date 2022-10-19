package com.cnasoft.health.auth.filter;

import com.cnasoft.health.common.context.TenantContextHolder;
import com.cnasoft.health.common.vo.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Map;

/**
 * token拦截器
 * 1:  赋值租户id
 * 2: 返回统一token格式
 *
 * @author cnasoft
 * @date 2020/7/3 8:49
 */
@Slf4j
@Component
@Aspect
public class OauthTokenAspect {

    @Around("execution(* org.springframework.security.oauth2.provider.endpoint.TokenEndpoint.postAccessToken(..))")
    public Object handleControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object[] args = joinPoint.getArgs();
            Principal principal = (Principal) args[0];
            if (!(principal instanceof Authentication)) {
                throw new InsufficientAuthenticationException(
                        "There is no client authentication. Try adding an appropriate authentication filter.");
            }
            String clientId = getClientId(principal);
            Map<String, String> parameters = (Map<String, String>) args[1];
            String grantType = parameters.get(OAuth2Utils.GRANT_TYPE);

            //保存租户id
            TenantContextHolder.setTenant(clientId);
            Object proceed = joinPoint.proceed();
            if ("authorization_code".equals(grantType)) {
                return proceed;
            } else {
                ResponseEntity<OAuth2AccessToken> responseEntity = (ResponseEntity<OAuth2AccessToken>) proceed;
                OAuth2AccessToken body = responseEntity.getBody();
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(CommonResult.success(body));
            }
        } catch (Exception e) {
            log.error("授权错误", e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(CommonResult.error(e.getMessage()));
        } finally {
            TenantContextHolder.clear();
        }
    }

    private String getClientId(Principal principal) {
        Authentication client = (Authentication) principal;
        if (!client.isAuthenticated()) {
            throw new InsufficientAuthenticationException("The client is not authenticated.");
        }
        String clientId = client.getName();
        if (client instanceof OAuth2Authentication) {
            clientId = ((OAuth2Authentication) client).getOAuth2Request().getClientId();
        }
        return clientId;
    }
}
