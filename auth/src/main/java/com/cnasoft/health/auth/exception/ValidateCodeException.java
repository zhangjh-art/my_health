package com.cnasoft.health.auth.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * 验证码异常
 *
 * @author cnasoft
 * @date 2020/7/2 21:27
 */
public class ValidateCodeException extends AuthenticationException {

    public ValidateCodeException(String msg) {
        super(msg);
    }
}
