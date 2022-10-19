package com.cnasoft.health.auth.tenant;

import com.cnasoft.health.common.context.TenantContextHolder;
import com.cnasoft.health.common.dto.SysUserDTO;
import com.cnasoft.health.common.token.TenantUsernamePasswordAuthenticationToken;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.userservice.feign.UserFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Map;

/**
 * /oauth/authorize拦截器
 * 解决不同租户单点登录时角色没变化
 *
 * @author cnasoft
 * @date 2020/7/3 13:08
 */
@Slf4j
@Component
@Aspect
public class OauthAuthorizeAspect {
    @Autowired
    private UserFeignClient userFeignClient;

    @Around("execution(* org.springframework.security.oauth2.provider.endpoint.AuthorizationEndpoint.authorize(..))")
    public Object doAroundMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Map<String, String> parameters = (Map<String, String>) args[1];
        Principal principal = (Principal) args[3];
        if (principal instanceof TenantUsernamePasswordAuthenticationToken) {
            TenantUsernamePasswordAuthenticationToken tenantToken = (TenantUsernamePasswordAuthenticationToken) principal;
            String clientId = tenantToken.getClientId();
            String requestClientId = parameters.get(OAuth2Utils.CLIENT_ID);
            //判断是否不同租户单点登录
            if (!requestClientId.equals(clientId)) {
                try {
                    TenantContextHolder.setTenant(requestClientId);
                    //重新查询对应该租户的角色等信息
                    CommonResult<SysUserDTO> result = userFeignClient.findUserByUsername(tenantToken.getName());
                    result.checkError();
                    SysUserDTO user = result.getData();
                    tenantToken = new TenantUsernamePasswordAuthenticationToken(user, tenantToken.getCredentials(), user.getAuthorities(), requestClientId);
                    args[3] = tenantToken;
                } finally {
                    TenantContextHolder.clear();
                }
            }
        }
        return joinPoint.proceed(args);
    }
}
