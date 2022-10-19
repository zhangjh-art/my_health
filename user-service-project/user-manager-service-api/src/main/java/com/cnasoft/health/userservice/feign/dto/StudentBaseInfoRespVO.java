package com.cnasoft.health.userservice.feign.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author Administrator
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@ApiModel("学生基础信息返回结果")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class StudentBaseInfoRespVO extends UserRespVO {

    @ApiModelProperty(value = "身份证号")
    private String identityCardNumber;

    @ApiModelProperty(value = "年级")
    private String grade;

    @ApiModelProperty(value = "班级")
    private Long clazzId;

    @ApiModelProperty(value = "班级名称")
    private String clazzName;

    @ApiModelProperty(value = "学号")
    private String studentNumber;

    @ApiModelProperty(value = "学籍号")
    private String studentCode;

    @ApiModelProperty(value = "学籍状态：1=在读，2=毕业，3=转校，4=休学，5=退学，6=肄业")
    private Integer studentStatus;
}
