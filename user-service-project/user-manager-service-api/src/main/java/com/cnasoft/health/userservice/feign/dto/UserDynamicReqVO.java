package com.cnasoft.health.userservice.feign.dto;

import com.cnasoft.health.common.annotation.NotBlankLength;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@ApiModel("保存/修改用户心情日志")
public class UserDynamicReqVO {
    public interface Update {
    }

    @NotNull(groups = UserDynamicReqVO.Update.class, message = "id不能为空")
    @ApiModelProperty(value = "id")
    private Long id;

    /**
     * 字数长度限制为100字以内
     */
    @ApiModelProperty(value = "心情内容，长度限制100字")
    @NotBlankLength(max = 100, message = "内容长度限制100字")
    private String content;

    @ApiModelProperty(value = "入睡时间")
    private Date sleepTime;

    @ApiModelProperty(value = "起床时间")
    private Date getupTime;

    @ApiModelProperty(value = "睡眠时长")
    private Integer sleepMinute;

    @ApiModelProperty(value = "身高,限制60cm~250厘米")
    @Min(value = 60, message = "身高最少60cm")
    @Max(value = 250, message = "身高最高250cm")
    private Double height;

    /**
     * 体重限制为10-200kg
     */
    @ApiModelProperty(value = "体重")
    @Min(value = 10, message = "体重最少10kg")
    @Max(value = 200, message = "体重最多200kg")
    private Double weight;

    @ApiModelProperty(value = "是否小憩，1：是；0：否")
    private Integer nap;

    @ApiModelProperty(value = "小憩时长,默认30分钟")
    private Integer napMinute = 30;

    @ApiModelProperty(value = "心情1开心2难过3大哭4高兴5悲伤")
    private Integer mood;

}
