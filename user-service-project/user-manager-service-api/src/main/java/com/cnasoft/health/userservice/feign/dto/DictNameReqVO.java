package com.cnasoft.health.userservice.feign.dto;

import com.cnasoft.health.common.util.io.serializer.DateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author zcb
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("字段管理请求实体")
public class DictNameReqVO implements Serializable {

    @ApiModelProperty(value = "字段名称")
    String dictName;

    @ApiModelProperty(value = "字段类型")
    String dictType;

    @ApiModelProperty(value = "状态，0启用，1禁用")
    Boolean disable;

    @ApiModelProperty(value = "审核状态")
    Integer approveStatus;

    @ApiModelProperty(value = "起始创建日期")
    @JsonDeserialize(using = DateDeserializer.class)
    Date startCreateTime;

    @ApiModelProperty(value = "截止创建日期")
    @JsonDeserialize(using = DateDeserializer.class)
    Date endCreateTime;

    Integer pageSize;

    Integer pageNum;

}