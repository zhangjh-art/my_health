package com.cnasoft.health.userservice.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 家庭情况分类
 *
 * @author zcb
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum FamilyCondition {
    NORMAL(1, "正常"),
    SINGLE(2, "单亲"),
    DIVORCE(3, "离异再婚"),
    DEAD(4, "丧亡学生"),
    NO_MARRIED(5, "非婚子女"),
    FUXING(6, "父母服刑"),
    XIDU(7, "吸毒人员家庭子女");

    @EnumValue
    private int code;
    private String name;

    FamilyCondition(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static FamilyCondition getFamilyCondition(final int code) {
        for (FamilyCondition condition : values()) {
            if (condition.getCode() == code) {
                return condition;
            }
        }

        return null;
    }

    public static FamilyCondition getFamilyCondition(final String name) {
        for (FamilyCondition condition : values()) {
            if (condition.getName().equalsIgnoreCase(name)) {
                return condition;
            }
        }

        return null;
    }
}
