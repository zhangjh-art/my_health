package com.cnasoft.health.userservice.feign.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author ganghe
 * @date 2022/4/14 12:07
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("修改校级管理员请求参数")
public class SchoolManagerUpdateReqVO extends SysUserReqVO {

    @ApiModelProperty(value = "用户id")
    @NotNull(message = "用户id不能为空")
    private Long id;

    @NotNull(message = "请选择区域")
    @ApiModelProperty(value = "区域ID", example = "110000")
    private Integer areaCode;

    @NotNull(message = "请选择学校")
    @ApiModelProperty(value = "学校ID", example = "1")
    private Long schoolId;

    private String schoolName;
}
