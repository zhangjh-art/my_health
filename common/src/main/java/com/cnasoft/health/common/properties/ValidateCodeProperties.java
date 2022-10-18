package com.cnasoft.health.common.properties;

import lombok.Getter;
import lombok.Setter;

/**
 * 验证码配置
 *
 * @author cnasoft
 * @date 2020/7/2 15:56
 */
@Setter
@Getter
public class ValidateCodeProperties {

    /**
     * 设置认证通时不需要验证码的clientId
     */
    private String[] ignoreClientCode = {};

}
