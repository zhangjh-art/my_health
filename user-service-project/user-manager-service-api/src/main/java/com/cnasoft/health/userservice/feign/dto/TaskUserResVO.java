package com.cnasoft.health.userservice.feign.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 任务选择用户返回数据
 *
 * @author zcb
 * @date 2020/8/14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("任务选择用户返回数据")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TaskUserResVO {

    private Long id;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "用户姓名")
    private String name;

    @ApiModelProperty(value = "身份证号")
    private String identityCardNumber;

    @ApiModelProperty(value = "学生信息，查询家长时需要返回")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<TaskUserResVO> students;

    @ApiModelProperty(value = "用户手机号")
    private String mobile;

    @ApiModelProperty(value = "性别(1男2女0未知)", required = true, dataType = "Integer", example = "2")
    private Integer sex;

    @ApiModelProperty(value = "部门")
    private String department;

    @ApiModelProperty(value = "部门名字")
    private String departmentName;

    @ApiModelProperty(value = "岗位")
    private String post;

    @ApiModelProperty(value = "工号")
    private String jobNumber;

    @ApiModelProperty(value = "学号")
    private String studentNumber;

    private Long parentId;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "班级id")
    private Long clazzId;

    @ApiModelProperty(value = "班级名称")
    private String clazzName;

    @ApiModelProperty(value = "年级code")
    private String grade;

    @ApiModelProperty(value = "年级名称")
    private String gradeName;

    @ApiModelProperty(value = "账号状态")
    private Boolean enabled;

    @ApiModelProperty(value = "激活状态")
    private Boolean isActive;

    @ApiModelProperty(value = "家长关系，1：父母；2：其他监护人")
    private Integer relationship;
}
