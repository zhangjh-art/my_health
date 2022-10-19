package com.cnasoft.health.userservice.feign.dto;

import com.cnasoft.health.common.annotation.NotBlankPattern;
import com.cnasoft.health.common.util.text.TextValidator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author ganghe
 * @date 2022/4/14 10:30
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("添加校级管理员请求参数")
public class SchoolManagerCreateReqVO extends SysUserReqVO {

    @ApiModelProperty(value = "手机号", required = true)
    @NotEmpty(message = "手机号不能为空")
    @NotBlankPattern(regexp = TextValidator.REGEX_MOBILE_EXACT, message = "手机号格式错误")
    private String mobile;

    @NotNull(message = "请选择区域")
    @ApiModelProperty(value = "区域ID", example = "110000")
    private Integer areaCode;

    @NotNull(message = "请选择学校")
    @ApiModelProperty(value = "学校ID", example = "1")
    private Long schoolId;
}