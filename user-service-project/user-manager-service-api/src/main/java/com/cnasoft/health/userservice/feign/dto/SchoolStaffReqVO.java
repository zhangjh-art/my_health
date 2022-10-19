package com.cnasoft.health.userservice.feign.dto;

import com.cnasoft.health.common.annotation.NotBlankLength;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 校教职工
 *
 * @author ganghe
 * @date 2022/4/23 15:14
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("校教职工新增/编辑请求参数")
public class SchoolStaffReqVO extends UserReqVO {

    @ApiModelProperty(value = "部门，长度限制20位")
    @NotEmpty(message = "部门不能为空", groups = {UserReqVO.Add.class})
    @NotBlankLength(max = 20, message = "部门长度限制20位")
    private String department;

    @ApiModelProperty(value = "岗位，长度限制20位")
    @NotBlankLength(max = 20, message = "岗位长度限制20位")
    private String post;

    @ApiModelProperty(value = "工号，长度限制20位")
    @NotBlankLength(max = 20, message = "工号长度限制20位")
    private String jobNumber;

    @ApiModelProperty(value = "教职工类型，1：教职工；2：班主任；3：领导;")
    @NotNull(message = "教职工类型不能为空", groups = {UserReqVO.Add.class})
    private Integer type;
}
