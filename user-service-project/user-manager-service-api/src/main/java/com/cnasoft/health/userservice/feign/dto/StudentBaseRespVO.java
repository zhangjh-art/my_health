package com.cnasoft.health.userservice.feign.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("学生基础信息返回结果")
public class StudentBaseRespVO {

    @ApiModelProperty(value = "学生id")
    private Long id;

    @ApiModelProperty(value = "用户")
    private Long userId;

    @ApiModelProperty(value = "学生姓名")
    private String name;

    @ApiModelProperty(value = "身份证号")
    private String identityCardNumber;

    @ApiModelProperty(value = "性别: 1: 男 2: 女")
    private Integer sex;

    @ApiModelProperty(value = "年级")
    private String grade;

    @ApiModelProperty(value = "学号")
    private String studentNumber;

    @ApiModelProperty(value = "学籍号")
    private String studentCode;

    @ApiModelProperty(value = "班级名称")
    private String clazzId;

    @ApiModelProperty(value = "班级名称")
    private String clazzName;

    @ApiModelProperty("学籍状态：1=在读，2=毕业，3=转校，4=休学，5=退学，6=肄业")
    private String studentStatus;

    @ApiModelProperty(value = "头像")
    private String headImgUrl;
}
