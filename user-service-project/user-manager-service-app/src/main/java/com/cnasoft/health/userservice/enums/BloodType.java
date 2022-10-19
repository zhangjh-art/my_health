package com.cnasoft.health.userservice.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 血型
 *
 * @author zcb
 */
public enum BloodType {
    /**
     * 血型A型
     */
    A(1, "A型"),
    /**
     * 血型B型
     */
    B(2, "B型"),
    /**
     * 血型O型
     */
    O(3, "O型"),
    /**
     * 血型AB型
     */
    AB(4, "AB型");

    @EnumValue
    private final int code;
    private final String name;

    BloodType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static BloodType getBloodType(final String name) {
        for (BloodType bloodType : values()) {
            if (bloodType.getName().equalsIgnoreCase(name)) {
                return bloodType;
            }
        }

        return null;
    }
}
