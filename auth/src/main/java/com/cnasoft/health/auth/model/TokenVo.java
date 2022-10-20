package com.cnasoft.health.auth.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author cnasoft
 * @date 2020/7/3 12:24
 */
@Data
public class TokenVo implements Serializable {
    /**
     * token的值
     */
    private String tokenValue;
    /**
     * 到期时间
     */
    private Date expiration;
    /**
     * 用户名
     */
    private String username;
    /**
     * 所属应用
     */
    private String clientId;
    /**
     * 授权类型
     */
    private String grantType;
}
