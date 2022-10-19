package com.cnasoft.health.userservice.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 特殊情况分类
 *
 * @author Administrator
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum SpecialCondition {
    CHUOXUE(1, "辍学"),
    FUDU(2, "复读"),
    PENKUN(3, "贫困生"),
    TESHU(4, "家庭情况特殊"),
    QITA(5, "其他");

    @EnumValue
    private int code;
    private String name;

    SpecialCondition(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static SpecialCondition getSpecialCondition(final int code) {
        for (SpecialCondition condition : values()) {
            if (condition.getCode() == code) {
                return condition;
            }
        }

        return null;
    }

    public static SpecialCondition getSpecialCondition(final String name) {
        for (SpecialCondition condition : values()) {
            if (condition.getName().equalsIgnoreCase(name)) {
                return condition;
            }
        }

        return null;
    }
}
