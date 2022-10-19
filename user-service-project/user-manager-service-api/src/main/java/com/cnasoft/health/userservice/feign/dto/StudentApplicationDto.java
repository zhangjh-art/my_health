package com.cnasoft.health.userservice.feign.dto;

import com.cnasoft.health.common.util.io.serializer.DateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("用户应用")
public class StudentApplicationDto {

    private int id;//id

    private Long userId;//用户id

    private String applicationName;// 应用名称

    private String applicationImg;// 应用图片

    private String applicationDesc;// 应用描述

    private String applicationUrl;// 应用描述

    @JsonSerialize(using = DateSerializer.class)
    private Date createTime;


    private Long updateBy;

    @JsonSerialize(using = DateSerializer.class)
    private Date updateTime;


    private Boolean isDeleted;
}
