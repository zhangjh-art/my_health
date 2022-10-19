package com.cnasoft.health.userservice.feign.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 任务用户为家长时返回的学生数据
 * @author zcb
 * @date 2020/8/14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("任务用户为家长时返回的学生数据")
public class TaskStudentResVO {

    @ApiModelProperty(value = "学生id")
    private Long userId;

    @ApiModelProperty(value = "学生姓名")
    private String name;

    @ApiModelProperty(value = "性别(1男2女)", required = true, dataType = "Integer", example = "2")
    private Integer sex;

    @ApiModelProperty(value = "学号")
    private String studentNumber;

    @ApiModelProperty(value = "班级名字")
    private String clazzName;


}
