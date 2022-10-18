package com.cnasoft.health.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("学校列表")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SchoolDTO implements Serializable {

    @ApiModelProperty(value = "学校id")
    private Long id;

    @ApiModelProperty(value = "学校名")
    private String name;

    @ApiModelProperty(value = "区域编码")
    private Integer areaCode;

    @ApiModelProperty(value = "审核状态")
    private Integer approveStatus;

    @ApiModelProperty(value = "区/县")
    private Integer distinct;

    @ApiModelProperty(value = "市")
    private Integer city;

    @ApiModelProperty(value = "省")
    private Integer province;

    @ApiModelProperty(value = "审核时的备注信息")
    private String remark;
}
