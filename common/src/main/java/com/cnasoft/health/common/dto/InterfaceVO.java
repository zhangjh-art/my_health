package com.cnasoft.health.common.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 **/
@Data
public class InterfaceVO {
    @ApiModelProperty(value = "接口地址")
    private String path;

    @ApiModelProperty(value = "接口方法")
    private String method;
}
