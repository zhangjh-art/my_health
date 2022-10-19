package com.cnasoft.health.userservice.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 家中排行
 *
 * @author Administrator
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum FamilyShort {
    FIRST((byte) 1, "大子女"),
    MIDDLE((byte) 2, "中间子女"),
    LAST((byte) 3, "最小子女");

    @EnumValue
    private int code;
    private String name;

    FamilyShort(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static FamilyShort getFamilyShort(final int code) {
        for (FamilyShort familyShort : values()) {
            if (familyShort.getCode() == code) {
                return familyShort;
            }
        }

        return null;
    }

    public static FamilyShort getFamilyShort(final String name) {
        for (FamilyShort familyShort : values()) {
            if (familyShort.getName().equalsIgnoreCase(name)) {
                return familyShort;
            }
        }

        return null;
    }
}
