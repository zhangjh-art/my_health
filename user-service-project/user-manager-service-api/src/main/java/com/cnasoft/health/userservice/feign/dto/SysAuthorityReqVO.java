package com.cnasoft.health.userservice.feign.dto;

import com.cnasoft.health.common.dto.InterfaceVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author cnasoft
 * @date 2020/8/16 15:07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SysAuthorityReqVO {

    public interface Update {
    }

    @ApiModelProperty(value = "id")
    @NotNull(groups = SysAuthorityReqVO.Update.class, message = "权限id不能为空")
    private Long id;

    @ApiModelProperty(value = "权限类型(1目录2菜单3操作权限)", required = true, example = "2")
    @NotNull(message = "权限类型不能为空")
    private Integer type;

    @ApiModelProperty(value = "权限名")
    @NotBlank(message = "权限名不能为空")
    private String name;

    @ApiModelProperty(value = "权限编码")
    private String code;

    @ApiModelProperty(value = "父级权限编码")
    private String parentCode;

    @ApiModelProperty(value = "请求接口", example = "[{\"path\":\"/api-user/user/info\",\"method\":\"GET\"}]")
    private InterfaceVO[] interfaces;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "启用状态：false禁用,true启用")
    private Boolean enabled;
}
