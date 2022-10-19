package com.cnasoft.health.userservice.feign.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("职员心理档案")
public class StaffMentalFileVO {
    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "部门")
    private String department;

    @ApiModelProperty(value = "岗位")
    private String post;

    @ApiModelProperty(value = "工号")
    private String jobNumber;

    @ApiModelProperty(value = "姓名")
    private String name;

    @ApiModelProperty(value = "性别(1男2女)")
    private Integer sex;

    @ApiModelProperty(value = "手机号")
    private String mobile;

    @ApiModelProperty(value = "头像")
    private String headImgUrl;
}
