package com.cnasoft.health.userservice.excel.encrypt;

import com.cnasoft.health.common.encryptor.EncryptorUtil;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 */
@Component(value = "rsaEncryptor")
public class RsaEncryptor implements IEncrypt{
    @Override
    public String encrypt(String value) {
        return EncryptorUtil.encrypt(value);
    }
}
