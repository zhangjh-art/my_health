package com.cnasoft.health.userservice.feign.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("家长基础信息")
public class ParentBaseRespVO {

    @ApiModelProperty(value = "家长id")
    private Long id;

    @ApiModelProperty(value = "用户")
    private Long userId;

    @ApiModelProperty(value = "性别(1男2女)")
    private Integer sex;

    @ApiModelProperty(value = "姓名")
    private String name;

    @ApiModelProperty(value = "手机号")
    private String mobile;

    @ApiModelProperty(value = "头像")
    private String headImgUrl;

    @ApiModelProperty(value = "学生信息")
    private List<StudentBaseRespVO> studentInfos;
}
