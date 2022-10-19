package com.cnasoft.health.userservice.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 咨询报告关注等级
 */
public enum ConsultWarningLevelEnum {
    LOW_WARNING(0, "一般关注"),
    MID_WARNING(1, "中度关注"),
    HIGH_WARNING(2, "高度关注");

    @EnumValue
    private final Integer code;
    private final String name;

    ConsultWarningLevelEnum(final Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

}
