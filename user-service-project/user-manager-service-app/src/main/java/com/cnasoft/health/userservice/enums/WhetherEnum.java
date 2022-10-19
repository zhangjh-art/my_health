package com.cnasoft.health.userservice.enums;

@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum WhetherEnum {

    TRUE((byte) 1, "是"),
    FALSE((byte) 0, "否");

    private Byte value;
    private String description;

    WhetherEnum(Byte value, String description) {
        this.value = value;
        this.description = description;
    }

    public Byte getValue() {
        return value;
    }

    public void setValue(Byte value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
