package com.cnasoft.health.userservice.feign.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author Administrator
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@ApiModel("添加区域心理教研员列表响应参数")
public class AreaTeacherRespVO extends UserRespVO implements Serializable {

    @ApiModelProperty(value = "职称")
    private String title;

    @ApiModelProperty(value = "部门")
    private String department;

    @ApiModelProperty(value = "岗位")
    private String post;

    @ApiModelProperty(value = "工号")
    private String jobNumber;

    @ApiModelProperty(value = "专业")
    private String major;

    @ApiModelProperty(value = "擅长")
    private String specialty;

    @ApiModelProperty(value = "是否承接任务")
    private Boolean isAcceptTask;

    @ApiModelProperty(value = "证书资质")
    private List<String> certificationFile;
}
