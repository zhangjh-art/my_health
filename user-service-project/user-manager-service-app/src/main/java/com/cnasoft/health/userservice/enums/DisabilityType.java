package com.cnasoft.health.userservice.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 残疾类型
 *
 * @author Administrator
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum DisabilityType {
    NONE(1, "无残疾"),
    EYE(2, "视力残疾"),
    LISTEN(3, "听力残疾"),
    SPEAK(4, "言语残疾"),
    LIMBS(5, "肢体残疾"),
    BRAINS(6, "智力残疾"),
    SPIRIT(7, "精神残疾");

    @EnumValue
    private int code;
    private String name;

    DisabilityType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static DisabilityType getDisabilityType(final int code) {
        for (DisabilityType type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }

        return null;
    }

    public static DisabilityType getDisabilityType(final String name) {
        for (DisabilityType type : values()) {
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }

        return null;
    }
}
