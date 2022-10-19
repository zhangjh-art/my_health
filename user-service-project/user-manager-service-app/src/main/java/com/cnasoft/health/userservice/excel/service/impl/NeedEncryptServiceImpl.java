package com.cnasoft.health.userservice.excel.service.impl;

import com.cnasoft.health.userservice.excel.encrypt.NeedEncrypt;
import com.cnasoft.health.userservice.excel.service.INeedEncryptService;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 */
@Component
public class NeedEncryptServiceImpl<T> implements INeedEncryptService<T> {
    @NeedEncrypt
    @Override
    public void encrypt(T t) {

    }
}
