package com.cnasoft.health.auth.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * @author: lgf
 * @created: 2022/5/27
 */
public class RoleSelectionException extends AuthenticationException {

    public RoleSelectionException(String msg) {
        super(msg);
    }
}
