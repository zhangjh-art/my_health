package com.cnasoft.health.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 测评任务对象所属角色枚举类
 *
 * @Created by lgf on 2022/3/28.
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum TaskTargetRoleEnum {
    STUDENT(0, "学生"),
    PARENTS(1, "家长"),
    SCHOOL_STAFF(2, "教职工"),
    REGION_STAFF(3, "区域职员");

    private final Integer code;
    private final String description;

    TaskTargetRoleEnum(int code, String description) {
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

    public static TaskTargetRoleEnum getRole(int code) {
        for (TaskTargetRoleEnum typeEnum : values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return STUDENT;
    }
}
