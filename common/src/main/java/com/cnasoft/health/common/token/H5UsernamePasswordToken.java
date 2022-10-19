package com.cnasoft.health.common.token;

import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * @author cnasoft
 * @date 2020/7/2 20:08
 */
public class H5UsernamePasswordToken extends UsernamePasswordAuthenticationToken {
    /**
     * 选择的用户id
     */
    @Getter
    private final Long choseUserId;

    public H5UsernamePasswordToken(Object principal, Object credentials, Long choseUserId) {
        super(principal, credentials);
        this.choseUserId = choseUserId;
    }

    public H5UsernamePasswordToken(Object principal, Object credentials,
                                   Collection<? extends GrantedAuthority> authorities, Long choseUserId) {
        super(principal, credentials, authorities);
        this.choseUserId = choseUserId;
    }
}
