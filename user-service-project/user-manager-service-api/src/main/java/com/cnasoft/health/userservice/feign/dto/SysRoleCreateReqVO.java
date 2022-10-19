package com.cnasoft.health.userservice.feign.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.Set;

/**
 * @author cnasoft
 * @date 2020/8/16 12:06
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SysRoleCreateReqVO {
    @NotEmpty(message = "角色名称不能为空")
    @ApiModelProperty(value = "角色名称", required = true)
    private String name;

    @NotEmpty(message = "角色编码不能为空")
    @ApiModelProperty(value = "角色编码", required = true)
    private String code;

    @ApiModelProperty(value = "权限id列表")
    private Set<String> authorities;
}
