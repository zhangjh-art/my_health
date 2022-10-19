package com.cnasoft.health.userservice.feign.dto;

import com.cnasoft.health.common.util.io.serializer.DateSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author zcb
 * @date 2022/6/2
 **/
@Data
@ApiModel("校心理老师动态预警列表返回数据")
public class DynamicWarningRespVO {

    @ApiModelProperty(value = "预警id")
    private Long id;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "姓名")
    private String name;

    @ApiModelProperty(value = "性别(1男2女)")
    private Integer sex;

    @ApiModelProperty(value = "年级")
    private String grade;

    @ApiModelProperty(value = "班级")
    private String clazz;

    @ApiModelProperty(value = "部门")
    private String department;

    @ApiModelProperty(value = "岗位")
    private String post;

    @ApiModelProperty(value = "预警关键词")
    private List<String> warnWordList;

    @JsonIgnore
    private String warnWords;

    @ApiModelProperty(value = "预警级别")
    private Integer warnGrade;

    @ApiModelProperty(value = "预警时间")
    @JsonSerialize(using = DateSerializer.class)
    private Date createTime;

    @ApiModelProperty(value = "处置结果：0：未处置；1：已处置；2：已面诊；3：已转诊；4：错误预警")
    private Integer dealResult;

    @ApiModelProperty(value = "处置描述")
    private String dealDescription;

    @ApiModelProperty(value = "内置角色编码")
    private String presetRoleCode;
}
