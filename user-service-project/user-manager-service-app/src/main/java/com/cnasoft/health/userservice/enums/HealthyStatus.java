package com.cnasoft.health.userservice.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 健康状况
 *
 * @author Administrator
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum HealthyStatus {
    BLANK(1, "未填"),
    HEALTHY(2, "健康或良好"),
    WEAK(3, "一般或较弱"),
    MANXING(4, "有慢性病"),
    DEFECT(5, "有生理缺陷"),
    DISABILITY(6, "残疾"),
    QITA(7, "其他");

    @EnumValue
    private int code;
    private String name;

    HealthyStatus(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static HealthyStatus getHealthyStatus(final int code) {
        for (HealthyStatus healthyStatus : values()) {
            if (healthyStatus.getCode() == code) {
                return healthyStatus;
            }
        }

        return null;
    }

    public static HealthyStatus getHealthyStatus(final String name) {
        for (HealthyStatus healthyStatus : values()) {
            if (healthyStatus.getName().equalsIgnoreCase(name)) {
                return healthyStatus;
            }
        }

        return null;
    }
}
