package com.cnasoft.health.userservice.feign.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * @author cnasoft
 * @date 2020/8/18 13:38
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SysRoleAuthorityCreateVO {
    @ApiModelProperty(value = "角色编码", required = true)
    @NotNull(message = "角色编码不能为空")
    private String roleCode;

    @ApiModelProperty(value = "权限编码列表", required = true)
    @NotNull(message = "权限编码列表不能为空")
    private Set<String> authorityCodes;
}
