package com.cnasoft.health.userservice.feign.dto;

import com.cnasoft.health.common.util.io.serializer.DateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("用户动态")
public class UserDynamicDTO {

    private Long id;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "心情内容")
    private String content;

    @ApiModelProperty(value = "入睡时间")
    private Date sleepTime;

    @ApiModelProperty(value = "起床时间")
    private Date getupTime;

    @ApiModelProperty(value = "睡眠时长")
    private Integer sleepMinute;

    @ApiModelProperty(value = "身高")
    private Double height;

    @ApiModelProperty(value = "体重")
    private Double weight;

    @ApiModelProperty(value = "是否小憩，1：是；0：否")
    private Integer nap;

    @ApiModelProperty(value = "小憩时长,默认30分钟")
    private Integer napMinute = 30;

    @ApiModelProperty(value = "心情1开心2难过3大哭4高兴5悲伤")
    private Integer mood;

    private Integer sort = 0;

    @ApiModelProperty(value = "是否预警，1：是；0：否")
    private Integer isWarn;

    @ApiModelProperty(value = "预警级别")
    private Integer warnGrade;

    @ApiModelProperty(value = "预警关键词")
    private String warnWords;

    @ApiModelProperty(value = "处置结果：0：未处置；1：已处置；2：已面诊；3：已转诊；4：错误预警")
    private Integer dealResult;

    @ApiModelProperty(value = "处置描述")
    private String dealDescription;

    @JsonSerialize(using = DateSerializer.class)
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @JsonSerialize(using = DateSerializer.class)
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

}
