package com.cnasoft.health.userservice.feign.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author cnasoft
 * @date 2020/8/14 9:42
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("修改用户请求参数")
public class SysUserUpdateReqVO extends SysUserReqVO {

    @ApiModelProperty(value = "用户id")
    @NotNull(message = "用户id不能为空")
    private Long id;

    /**
     * 姓名修改次数
     */
    private Integer nameChange;
}
