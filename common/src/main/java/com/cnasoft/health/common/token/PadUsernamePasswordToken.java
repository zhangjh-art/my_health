package com.cnasoft.health.common.token;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * PAD测试管理员用户名和密码登录
 *
 * @author ganghe
 * @date 2020/7/21 13:56
 */
public class PadUsernamePasswordToken extends UsernamePasswordAuthenticationToken {

    public PadUsernamePasswordToken(Object principal, Object credentials) {
        super(principal, credentials);
    }

    public PadUsernamePasswordToken(Object principal, Object credentials,
                                    Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }
}
