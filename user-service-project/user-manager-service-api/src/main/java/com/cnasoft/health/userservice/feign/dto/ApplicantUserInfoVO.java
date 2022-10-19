package com.cnasoft.health.userservice.feign.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 申请人信息VO
 *
 * @Created by lgf on 2022/4/21.
 */
@Data
public class ApplicantUserInfoVO {

    @ApiModelProperty(value = "id")
    private String id;

    @ApiModelProperty(value = "用户名称")
    private String name;
}
