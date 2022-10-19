package com.cnasoft.health.userservice.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 是否留守儿童
 *
 * @author ganghe
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum LeftBehindChildren {
    NOT_LEFT_BEHIND(1, "非留守儿童"),
    SINGLE_LEFT_BEHIND(2, "单亲留守儿童"),
    DOUBLE_LEFT_BEHIND(3, "双亲留守儿童");

    @EnumValue
    private int code;
    private String name;

    LeftBehindChildren(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static LeftBehindChildren getLeftBehindChildren(final int code) {
        for (LeftBehindChildren children : values()) {
            if (children.getCode() == code) {
                return children;
            }
        }

        return null;
    }

    public static LeftBehindChildren getLeftBehindChildren(final String name) {
        for (LeftBehindChildren children : values()) {
            if (children.getName().equalsIgnoreCase(name)) {
                return children;
            }
        }

        return null;
    }
}
