package com.cnasoft.health.userservice.enums;

/**
 * 1：学生；2：家长；3：校教职工；4：校心理老师；5：区域职员；6：区域心理教研员
 *
 * @author Administrator
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum ImportTypeEnum {
    STUDENT(1, "学生"),
    PARENT(2, "家长"),
    SCHOOL_STAFF(3, "校教职工"),
    SCHOOL_TEACHER(4, "校心理老师"),
    AREA_STAFF(5, "区域职员"),
    AREA_TEACHER(6, "区域心理教研员"),
    CLAZZ(7, "班级");

    private Integer code;
    private String name;

    ImportTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static ImportTypeEnum getImportType(final Integer code) {
        for (ImportTypeEnum importType : values()) {
            if (importType.getCode().equals(code)) {
                return importType;
            }
        }

        return null;
    }

    public static ImportTypeEnum getImportType(final String name) {
        for (ImportTypeEnum importType : values()) {
            if (importType.getName().equalsIgnoreCase(name)) {
                return importType;
            }
        }

        return null;
    }
}
