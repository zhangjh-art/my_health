package com.cnasoft.health.userservice.feign.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author Administrator
 * @date 2022/4/18 15:20
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("敏感词修改请求参数")
public class SensitiveWordUpdateReqVO {

    @ApiModelProperty(value = "id")
    @NotNull(message = "id不能为空")
    private Long id;

    @ApiModelProperty(value = "敏感词")
    private String text;
}
