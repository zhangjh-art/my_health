package com.cnasoft.health.userservice.feign.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("咨询小精灵问题")
public class SmartConsultQuestionDTO {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "编号")
    private Integer sort;

    @ApiModelProperty(value = "问题")
    private String question;

    /*@ApiModelProperty(value = "答案")
    private String answer;*/
}
