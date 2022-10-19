package com.cnasoft.health.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Administrator
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum RoleEnum {
    admin(1L, "超级管理员", "ADMIN"),
    first_level_admin(2L, "一级管理员", "first_level_admin"),
    second_level_admin(3L, "二级管理员", "second_level_admin"),
    region_leader(4L, "区域领导", "region_leader"),
    region_admin(5L, "区域管理员", "region_admin"),
    region_psycho_teacher(6L, "区域心理教研员", "region_psycho_teacher"),
    region_staff(7L, "区域职员", "region_staff"),
    school_leader(8L, "校领导", "school_leader"),
    school_admin(9L, "校级管理员", "school_admin"),
    school_psycho_teacher(10L, "校心理教师", "school_psycho_teacher"),
    school_head_teacher(11L, "班主任", "school_head_teacher"),
    school_staff(12L, "校教职工", "school_staff"),
    parents(13L, "学生家长", "parents"),
    student(14L, "学生", "student"),
    test_admin(15L, "测试管理员", "test_admin");


    @EnumValue
    private final Long code;
    private final String name;
    private final String value;

    RoleEnum(final Long code, String name, String value) {
        this.code = code;
        this.name = name;
        this.value = value;
    }

    public Long getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    /**
     * 是否是内置角色
     *
     * @param value
     * @return
     */
    public static Boolean presetRole(String value) {
        for (RoleEnum roleEnum : values()) {
            if (roleEnum.getValue().equalsIgnoreCase(value)) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    public static List<String> getValueByTaskRole(Integer TaskTargetRoleEnumCode) {
        if (TaskTargetRoleEnumCode.equals(TaskTargetRoleEnum.STUDENT.getCode())){
            return Collections.singletonList(RoleEnum.student.getValue());
        }
        if (TaskTargetRoleEnumCode.equals(TaskTargetRoleEnum.PARENTS.getCode())){
            return Collections.singletonList(RoleEnum.parents.getValue());
        }
        if (TaskTargetRoleEnumCode.equals(TaskTargetRoleEnum.SCHOOL_STAFF.getCode())){
            List<String> roles = new ArrayList<>();
            roles.add(RoleEnum.school_staff.getValue());
            roles.add(RoleEnum.school_psycho_teacher.getValue());
            roles.add(RoleEnum.school_head_teacher.getValue());
            roles.add(RoleEnum.school_leader.getValue());
            roles.add(RoleEnum.school_admin.getValue());
            return roles;
        }
        if (TaskTargetRoleEnumCode.equals(TaskTargetRoleEnum.REGION_STAFF.getCode())){
            List<String> roles = new ArrayList<>();
            roles.add(RoleEnum.region_staff.getValue());
            roles.add(RoleEnum.region_psycho_teacher.getValue());
            roles.add(RoleEnum.school_head_teacher.getValue());
            roles.add(RoleEnum.region_leader.getValue());
            roles.add(RoleEnum.region_admin.getValue());
            return roles;
        }
        return new ArrayList<>();
    }
}
