package com.cnasoft.health.userservice.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 政治面貌
 *
 * @author Administrator
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum PoliticsStatus {
    DANGYUAN(1, "中共党员"),
    TUANYUAN(2, "共青团员"),
    MINZHU(3, "民主党派"),
    QUNZHONG(4, "群众"),
    QITA(5, "其他");

    @EnumValue
    private int code;
    private String name;

    PoliticsStatus(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static PoliticsStatus getPoliticsStatus(final int code) {
        for (PoliticsStatus politicsStatus : values()) {
            if (politicsStatus.getCode() == code) {
                return politicsStatus;
            }
        }

        return null;
    }

    public static PoliticsStatus getPoliticsStatus(final String name) {
        for (PoliticsStatus politicsStatus : values()) {
            if (politicsStatus.getName().equalsIgnoreCase(name)) {
                return politicsStatus;
            }
        }

        return null;
    }
}
