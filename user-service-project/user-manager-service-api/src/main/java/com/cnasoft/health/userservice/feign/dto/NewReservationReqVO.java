package com.cnasoft.health.userservice.feign.dto;

import javax.validation.constraints.NotNull;

import com.cnasoft.health.common.util.io.serializer.DateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("预约请求VO")
public class NewReservationReqVO {

    public interface Create {
    }

    public interface Update {
    }

    @NotNull(groups = Update.class, message = "id不能为空")
    private Long id;

    @ApiModelProperty(value = "0:待确认 1:已确认 2:已完成 3:已拒绝 4:已取消")
    private Integer status;

    @NotNull(groups = Create.class, message = "咨询师ID不能为空")
    @ApiModelProperty(value = "咨询师id", required = true)
    private Long psychiatristId;

    @ApiModelProperty(value = "预约人id 代预约时不能为空", required = true)
    private Long userId;

    @ApiModelProperty(value = "用户角色类型 0:学生 1:家长 2:教职工")
    private Integer userRoleType;

    @NotNull(groups = Create.class, message = "预约日期不能为空")
    @ApiModelProperty(value = "预约日期", required = true)
    @JsonDeserialize(using = DateDeserializer.class)
    private Date date;

    @NotNull(groups = Create.class, message = "预约开始时间不能为空")
    @ApiModelProperty(value = "预约开始时间 ex: 0900", required = true)
    private String startTime;

    @NotNull(groups = Create.class, message = "预约结束时间不能为空")
    @ApiModelProperty(value = "预约结束时间 ex: 1000", required = true)
    private String endTime;

    @NotNull(groups = Create.class, message = "咨询问题不能为空")
    @ApiModelProperty(value = "咨询问题: 逗号隔开", required = true)
    private String consultTypes;

    @ApiModelProperty(value = "咨询问题补充说明")
    private String consultDescription;

    @ApiModelProperty(value = "咨询问题症状描述")
    private String description;

    @ApiModelProperty(value = "预约备注")
    private String remark;

    @ApiModelProperty(value = "补充问题1")
    private String question1;

    @ApiModelProperty(value = "补充问题2")
    private String question2;

    @ApiModelProperty(value = "补充问题3")
    private String question3;

    @ApiModelProperty(value = "补充问题4")
    private String question4;

    @ApiModelProperty(value = "补充问题5")
    private String question5;
}
