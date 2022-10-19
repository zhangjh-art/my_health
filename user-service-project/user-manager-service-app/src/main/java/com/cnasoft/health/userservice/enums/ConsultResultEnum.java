package com.cnasoft.health.userservice.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 干预结果 0 跟踪观察 1 解除关注 2 需要转介
 */
public enum ConsultResultEnum {
    HANDLED_FOLLOW(0, "跟踪观察"), HANDLED_DOWN(1, "解除关注"), UNABLE_HANDLED(2, "需要转介");

    private final Integer code;
    private final String description;

    ConsultResultEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @JsonValue
    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
