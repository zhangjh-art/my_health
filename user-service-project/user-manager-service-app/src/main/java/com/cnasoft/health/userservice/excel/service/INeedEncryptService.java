package com.cnasoft.health.userservice.excel.service;

/**
 * @author Administrator
 */
public interface INeedEncryptService<T> {
    /**
     * 对需要加密的字段加密，切面方法
     *
     * @param t
     */
    void encrypt(T t);
}
