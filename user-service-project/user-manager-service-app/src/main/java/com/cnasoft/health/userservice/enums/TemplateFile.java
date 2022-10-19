package com.cnasoft.health.userservice.enums;


/**
 * 模板文件枚举
 *
 * @author zcb
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum TemplateFile {

    STUDENT(1, "学生导入模板.xls"),
    PARENT(2, "家长导入模板.xls"),
    SCHOOL_STAFF(3, "校教职工导入模板.xls"),
    SCHOOL_TEACHER(4, "校心理老师导入模板.xls"),
    AREA_STAFF(5, "区域职员导入模板.xls"),
    AREA_TEACHER(6, "区域心理教研员导入模板.xls"),
    CLAZZ(7, "班级导入模板.xls"),
    GAUGE(8, "量表导入模板.xlsx");

    private Integer value;
    private String description;

    TemplateFile(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static TemplateFile getTemplateFile(final Integer value) {
        for (TemplateFile templateFile : values()) {
            if (templateFile.getValue().equals(value)) {
                return templateFile;
            }
        }

        return null;
    }

    public static TemplateFile getTemplateFile(final String description) {
        for (TemplateFile templateFile : values()) {
            if (templateFile.getDescription().equals(description)) {
                return templateFile;
            }
        }

        return null;
    }

}
