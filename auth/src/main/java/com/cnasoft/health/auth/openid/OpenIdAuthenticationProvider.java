package com.cnasoft.health.auth.openid;

import com.cnasoft.health.auth.service.CnaSoftUserDetailService;
import com.cnasoft.health.common.token.OpenIdAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author cnasoft
 * @date 2020/7/2 19:52
 */
public class OpenIdAuthenticationProvider implements AuthenticationProvider {
    private CnaSoftUserDetailService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) {
        OpenIdAuthenticationToken authenticationToken = (OpenIdAuthenticationToken) authentication;
        String openId = (String) authenticationToken.getPrincipal();
        UserDetails user = userDetailsService.loadUserByUsername("admin");
        if (user == null) {
            throw new InternalAuthenticationServiceException("openId错误");
        }
        OpenIdAuthenticationToken authenticationResult = new OpenIdAuthenticationToken(user, user.getAuthorities());
        authenticationResult.setDetails(authenticationToken.getDetails());
        return authenticationResult;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OpenIdAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public void setUserDetailsService(CnaSoftUserDetailService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
}
