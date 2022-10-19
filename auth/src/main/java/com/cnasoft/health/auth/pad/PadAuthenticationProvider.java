package com.cnasoft.health.auth.pad;

import com.cnasoft.health.auth.service.CnaSoftUserDetailService;
import com.cnasoft.health.common.token.PadAuthenticationToken;
import lombok.Setter;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * PAD学生扫码登录-提供者
 *
 * @author ganghe
 * @date 2022/7/19 19:52
 */
@Setter
public class PadAuthenticationProvider implements AuthenticationProvider {
    private CnaSoftUserDetailService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) {
        PadAuthenticationToken authenticationToken = (PadAuthenticationToken) authentication;
        Long userId = (Long) authenticationToken.getPrincipal();
        UserDetails user = userDetailsService.findUserById(userId);
        if (user == null) {
            throw new InternalAuthenticationServiceException("用户ID错误");
        }

        return new PadAuthenticationToken(user, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PadAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
