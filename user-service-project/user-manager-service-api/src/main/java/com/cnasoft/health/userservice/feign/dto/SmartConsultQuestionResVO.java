package com.cnasoft.health.userservice.feign.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("咨询小精灵问题列表")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SmartConsultQuestionResVO {
    @ApiModelProperty(value = "问题类型")
    private String type;

    @ApiModelProperty(value = "问题类型列表")
    private List<SmartConsultQuestionResVO> questionRes;

    @ApiModelProperty(value = "问题详情列表")
    private List<SmartConsultQuestionDTO> questionInfos;
}
