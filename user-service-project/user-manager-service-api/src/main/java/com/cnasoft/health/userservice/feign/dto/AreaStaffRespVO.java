package com.cnasoft.health.userservice.feign.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * @author Administrator
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@ApiModel("添加区域职员列表响应参数")
public class AreaStaffRespVO extends UserRespVO implements Serializable {

    @ApiModelProperty(value = "工号")
    private String jobNumber;

    @ApiModelProperty(value = "部门")
    private String department;

    @ApiModelProperty(value = "岗位")
    private String post;

    @ApiModelProperty(value = "类型：1 职员，2 领导")
    private Integer type;
}
