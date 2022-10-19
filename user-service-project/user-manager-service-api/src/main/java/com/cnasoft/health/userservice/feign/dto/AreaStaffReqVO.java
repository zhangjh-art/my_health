package com.cnasoft.health.userservice.feign.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import com.cnasoft.health.common.annotation.NotBlankLength;

/**
 * @author Administrator
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("区域职员新增/修改请求参数")
public class AreaStaffReqVO extends UserReqVO {

    @ApiModelProperty(value = "类型：1 职员，2 领导")
    private Integer type;

    @ApiModelProperty(value = "部门，长度限制20位")
    @NotBlankLength(max = 20, message = "部门长度限制20位")
    private String department;

    @ApiModelProperty(value = "岗位，长度限制20位")
    @NotBlankLength(max = 20, message = "岗位长度限制20位")
    private String post;

    @ApiModelProperty(value = "工号，长度限制20位")
    @NotBlankLength(max = 20, message = "工号长度限制20位")
    private String jobNumber;
}
