package com.cnasoft.health.userservice.feign.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 校教职工
 *
 * @author ganghe
 * @date 2022/4/19 16:29
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SchoolStaffRespVO extends UserRespVO implements Serializable {

    @ApiModelProperty(value = "部门")
    private String department;

    @ApiModelProperty(value = "岗位")
    private String post;

    @ApiModelProperty(value = "工号")
    private String jobNumber;

    @ApiModelProperty(value = "教职工类型，1：教职工；2：班主任；3：领导;")
    private Integer type;

}
