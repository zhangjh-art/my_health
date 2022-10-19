package com.cnasoft.health.userservice.feign.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@ApiModel("咨询请求VO")
public class ConsultationRecordReqVO {
    public interface Create {
    }

    private Long userId;

    private Integer source;

    private Long sourceId;

    private String consultTypes;

    private Date date;
}
