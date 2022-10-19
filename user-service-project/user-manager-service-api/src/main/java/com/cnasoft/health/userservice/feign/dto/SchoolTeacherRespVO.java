package com.cnasoft.health.userservice.feign.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 校心理老师
 *
 * @author ganghe
 * @date 2022/4/19 16:29
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SchoolTeacherRespVO extends UserRespVO implements Serializable {

    @ApiModelProperty(value = "职称")
    private String title;

    @ApiModelProperty(value = "专业")
    private String major;

    @ApiModelProperty(value = "擅长")
    private String specialty;

    @ApiModelProperty(value = "工号")
    private String jobNumber;

    @ApiModelProperty(value = "岗位")
    private String post;

    @ApiModelProperty(value = "部门")
    private String department;

    @ApiModelProperty(value = "是否承接任务")
    private Boolean isAcceptTask;

    @ApiModelProperty(value = "证书资质")
    private List<String> certificationFile;
}
