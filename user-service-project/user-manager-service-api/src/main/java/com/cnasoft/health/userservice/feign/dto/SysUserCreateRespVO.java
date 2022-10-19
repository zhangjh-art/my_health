package com.cnasoft.health.userservice.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

/**
 * @author cnasoft
 * @date 2020/8/14 9:47
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SysUserCreateRespVO {

    /**
     * 用户名
     */
    @NotBlank
    private String username;

    /**
     * 昵称
     */
    @NotEmpty
    private String nickname;
}
