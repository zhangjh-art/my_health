package com.cnasoft.health.userservice.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ganghe
 * @date 2022/3/28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SysUserAuthorityReqVO {
    /**
     * 权限ID
     */
    private String authorityCode;

    /**
     * 是否拥有权限 0: 否, 1: 是
     */
    private Boolean isOwned;
}
