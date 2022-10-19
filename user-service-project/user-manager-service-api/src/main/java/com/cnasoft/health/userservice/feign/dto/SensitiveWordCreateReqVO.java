package com.cnasoft.health.userservice.feign.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @author Administrator
 * @date 2022/4/18 15:20
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("敏感词新增请求参数")
public class SensitiveWordCreateReqVO {

    @NotBlank(message = "敏感词不能为空")
    @ApiModelProperty(value = "敏感词")
    private String text;
}
