package com.cnasoft.health.userservice.excel.service;

public interface INeedDictService<T> {
    /**
     * 对需要从数据字典取数据，切面方法
     * @param t
     */
    void dictValue(T t);
}
