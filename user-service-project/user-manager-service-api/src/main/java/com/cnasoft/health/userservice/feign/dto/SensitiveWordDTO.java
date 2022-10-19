package com.cnasoft.health.userservice.feign.dto;

import com.cnasoft.health.common.util.io.serializer.DateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 敏感词库
 *
 * @author ganghe
 * @date 2022/4/18 14:56
 **/
@Data
@Accessors(chain = true)
public class SensitiveWordDTO implements Serializable {
    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "敏感词")
    private String text;

    @ApiModelProperty(value = "创建人姓名")
    private String createByName;

    @JsonSerialize(using = DateSerializer.class)
    @ApiModelProperty(value = "创建时间,时间戳(秒)")
    private Date createTime;
}
