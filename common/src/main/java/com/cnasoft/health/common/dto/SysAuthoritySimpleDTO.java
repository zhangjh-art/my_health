package com.cnasoft.health.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SysAuthoritySimpleDTO implements Serializable {

    /**
     * 角色id
     */
    private Long roleId;

    /**
     * 权限code
     */
    private String code;

    /**
     * 请求接口
     */
    private String interfaces;
}