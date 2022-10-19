package com.cnasoft.health.userservice.feign.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @author Administrator
 */
@Data
@Builder
public class BaseSmsCreateRespVO {
    @ApiModelProperty(value = "短信发送结果状态", required = true, example = "0")
    private Integer msgId;

    @ApiModelProperty(value = "短信发送结果详情", required = true, example = "操作成功")
    private String msg;
}
