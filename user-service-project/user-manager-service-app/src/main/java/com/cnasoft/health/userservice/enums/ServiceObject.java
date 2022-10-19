package com.cnasoft.health.userservice.enums;

/**
 * @author zcb
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum ServiceObject {
    STUDENT( 0, "学生"),
    PARENT( 1, "家长"),
    SCHOOL_STAFF( 2, "校教职工"),
    AREA_STAFF( 3, "区域职员");

    private Integer value;
    private String description;

    ServiceObject(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getDescription(){
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
