package com.cnasoft.health.userservice.feign.dto;

import com.cnasoft.health.common.util.io.serializer.DateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("咨询记录列表响应参数")
public class ConsultationRecordRespVO {

    @ApiModelProperty(value = "咨询方式 0:求助小精灵 1:在线咨询")
    private Integer source;

    @ApiModelProperty(value = "咨询问题类型")
    private String consultTypes;

    @ApiModelProperty(value = "日期")
    @JsonSerialize(using = DateSerializer.class)
    private Date date;

    @ApiModelProperty(value = "咨询问题")
    private String consultQuestion;
}
