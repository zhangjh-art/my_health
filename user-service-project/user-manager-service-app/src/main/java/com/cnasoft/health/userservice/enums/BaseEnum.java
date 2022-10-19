package com.cnasoft.health.userservice.enums;

/**
 * @author zcb
 */
public interface BaseEnum<E extends Enum<?>, T> {

    /**
     * 获取枚举值
     * @return  枚举值
     */
    T getValue();

    /**
     * 获取枚举值描述
     * @return  枚举值描述
     */
    String getDescription();
}
