package com.cnasoft.health.common.exception;

/**
 * 错误码对象
 * 全局错误码 [0, 999] see {@link }
 * 业务异常错误码 [1 000 000 000, +∞]
 */
public class ErrorCode {
    /**
     * 错误码
     */
    private final Integer code;
    /**
     * 错误提示
     */
    private final String message;

    public ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
