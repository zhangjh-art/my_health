package com.cnasoft.health.userservice.feign.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 学生列表返回数据
 *
 * @author zcb
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class StudentRespVO extends UserRespVO implements Serializable {

    @ApiModelProperty(value = "学号")
    private String studentNumber;

    @ApiModelProperty(value = "身份证号")
    private String identityCardNumber;

    @ApiModelProperty(value = "年级")
    private String grade;

    @ApiModelProperty(value = "班级")
    private Long clazzId;

    @ApiModelProperty(value = "班级名称")
    private String clazzName;

    @ApiModelProperty(value = "性别(1男2女)")
    private Integer sex;
    
    @ApiModelProperty(value = "是否逻辑删除0: 否, 1: 是")
    private Boolean clazzDeleted;

    @ApiModelProperty(value = "家长关系")
    private Integer relationship;

    @ApiModelProperty(value = "家长姓名")
    private String parentName;

    @ApiModelProperty(value = "家长电话")
    private String parentMobile;

    @ApiModelProperty(value = "是否逻辑删除0: 否, 1: 是")
    private Boolean isDeleted;
}
