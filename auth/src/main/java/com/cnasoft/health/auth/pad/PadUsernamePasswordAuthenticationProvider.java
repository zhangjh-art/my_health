package com.cnasoft.health.auth.pad;

import com.cnasoft.health.auth.service.CnaSoftUserDetailService;
import com.cnasoft.health.common.dto.SysUserDTO;
import com.cnasoft.health.common.token.PadUsernamePasswordToken;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;

import static com.cnasoft.health.userservice.constant.UserErrorCodeConstants.USER_PASSWORD_ERROR;

/**
 * PAD测试管理员用户名和密码登录-提供者
 *
 * @author ganghe
 * @date 2020/7/21 13:57
 */
@Slf4j
public class PadUsernamePasswordAuthenticationProvider implements AuthenticationProvider {

    @Setter
    private PasswordEncoder passwordEncoder;

    private CnaSoftUserDetailService userDetailService;

    public void setUserDetailService(UserDetailsService userDetailsService) {
        userDetailService = (CnaSoftUserDetailService)userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        PadUsernamePasswordToken authenticationToken = (PadUsernamePasswordToken)authentication;
        String username = (String)authenticationToken.getPrincipal();
        SysUserDTO user = userDetailService.findUserByUsernameWithTestManager(username);
        if (Objects.isNull(user)) {
            throw new InternalAuthenticationServiceException(USER_PASSWORD_ERROR.getMessage());
        }

        if (Objects.isNull(authentication.getCredentials())) {
            log.debug("未填写登陆密码");
            throw new BadCredentialsException(USER_PASSWORD_ERROR.getMessage());
        }

        String password = authentication.getCredentials().toString();
        password = passwordEncoder.encode(password);
        if (!userDetailService.pwdCheck(user, password)) {
            // 密码错误
            throw new BadCredentialsException(USER_PASSWORD_ERROR.getMessage());
        }

        return new PadUsernamePasswordToken(user, authentication.getCredentials(), user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PadUsernamePasswordToken.class.isAssignableFrom(authentication);
    }
}
