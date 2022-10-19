package com.cnasoft.health.userservice.feign.dto;

import com.cnasoft.health.common.util.io.serializer.DateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Created by lgf on 2022/4/6.
 */
@Data
public class MessageResVO {

    @ApiModelProperty(value = "消息id")
    private String id;

    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value = "消息类型")
    private Integer type;

    @ApiModelProperty(value = "消息内容")
    private String content;

    @ApiModelProperty(value = "消息参数")
    private String params;

    @ApiModelProperty(value = "是否已读")
    private Boolean hasRead;

    @ApiModelProperty(value = "创建人id")
    private String createBy;

    @ApiModelProperty(value = "创建人名称")
    private String createByUsername;

    @ApiModelProperty(value = "创建时间")
    @JsonSerialize(using = DateSerializer.class)
    private Date createTime;
}
