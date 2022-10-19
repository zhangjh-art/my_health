package com.cnasoft.health.userservice.feign.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 任务选择用户查询条件
 *
 * @author zcb
 * @date 2020/8/14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("任务选择用户查询条件")
public class TaskUserReqVO {

    @ApiModelProperty(value = "测评对象类型，0：学生；1：家长；2：校教职工；3：区域职员")
    @NotNull(message = "测评对象不能为空")
    private Integer serviceObject;

    @ApiModelProperty(value = "用户姓名")
    private String name;

    @ApiModelProperty(value = "学生姓名")
    private String studentName;

    @ApiModelProperty(value = "学号")
    private String studentNumber;

    @ApiModelProperty(value = "区域码值")
    private Integer areaCode;

    @ApiModelProperty(value = "年级码值集合")
    private List<String> grades;

    @ApiModelProperty(value = "班级id集合")
    private List<Long> clazzIds;

    @ApiModelProperty(value = "排除用户id集合")
    private List<Long> exceptUserIds;

    @ApiModelProperty(value = "学校id")
    private Long schoolId;

    @ApiModelProperty(value = "部门码值集合", dataType = "List")
    private List<String> departments;

    @ApiModelProperty(value = "岗位集合", dataType = "List")
    private List<String> posts;

    @ApiModelProperty(value = "混合输入框")
    private String mixInput;

    private Integer searchType;

    @ApiModelProperty(value = "页码")
    private Integer pageNum;

    @ApiModelProperty(value = "单页条数")
    private Integer pageSize;

    @ApiModelProperty(value = "手机号，区域职员名单确认时用")
    private String mobile;

    @JsonIgnore
    private List<Long> parentIds;
}
