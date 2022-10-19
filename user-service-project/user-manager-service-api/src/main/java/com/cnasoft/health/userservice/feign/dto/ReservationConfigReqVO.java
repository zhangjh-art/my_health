package com.cnasoft.health.userservice.feign.dto;

import com.cnasoft.health.common.util.io.serializer.DateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@ApiModel("预约设置请求VO")
public class ReservationConfigReqVO {

    public interface Create {
    }

    public interface Update {
    }

    @NotNull(groups = ReservationConfigReqVO.Update.class, message = "id不能为空")
    private Long id;

    private Long userId;

    @NotNull(groups = ReservationConfigReqVO.Create.class, message = "是否自动确认不能为空")
    @ApiModelProperty(value = "是否自动确认，1：是；0：否", required = true)
    private Integer autoConfirm;

    @NotNull(groups = ReservationConfigReqVO.Create.class, message = "到期日期不能为空")
    @ApiModelProperty(value = "设置有效日期", required = true)
    @JsonDeserialize(using = DateDeserializer.class)
    private Date expireDate;

    @NotNull(groups = ReservationConfigReqVO.Create.class, message = "单次出诊时间不能为空")
    @ApiModelProperty(value = "单次出诊时间 分钟/次", required = true)
    private Integer intervalTime;

    @NotNull(groups = ReservationConfigReqVO.Create.class, message = "单次问诊人数不能为空")
    @ApiModelProperty(value = "单次问诊人数 人/次", required = true)
    private Integer intervalNum;

    @NotNull(groups = ReservationConfigReqVO.Create.class, message = "提前预约时间不能为空")
    @ApiModelProperty(value = "提前预约时间(天)", required = true)
    private Integer advanceTime;

    @NotNull(groups = ReservationConfigReqVO.Create.class, message = "时段设置不能为空")
    @ApiModelProperty(value = "时段设置 {\"1\":[\"0900\",\"1100\" ...], \"2\": ...}", required = true)
    private Map<Integer, List<Map<String, String>>> timeConfig;
}
