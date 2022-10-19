package com.cnasoft.health.userservice.feign.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.cnasoft.health.common.annotation.NotBlankLength;

import java.util.List;

/**
 * @author Administrator
 * @date 2022/4/23 11:07
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("校心理老师新增/编辑请求参数")
public class SchoolTeacherReqVO extends UserReqVO {

    @ApiModelProperty(value = "职称，长度限制20位")
    @NotBlankLength(max = 20, message = "职称长度限制20位")
    private String title;

    @ApiModelProperty(value = "专业，长度限制20位")
    @NotBlankLength(max = 20, message = "专业长度限制20位")
    private String major;

    @ApiModelProperty(value = "擅长，长度限制200位")
    @NotBlankLength(max = 200, message = "擅长长度限制200位")
    private String specialty;

    @ApiModelProperty(value = "工号，长度限制20位")
    @NotBlankLength(max = 20, message = "工号长度限制20位")
    private String jobNumber;

    @ApiModelProperty(value = "岗位")
    private String post;

    @ApiModelProperty(value = "部门，长度限制20位")
    @NotBlankLength(max = 20, message = "部门长度限制20位")
    private String department;

    @ApiModelProperty(value = "是否承接任务")
    private Boolean isAcceptTask;

    @ApiModelProperty(value = "证书资质")
    private List<String> certificationFile;
}
