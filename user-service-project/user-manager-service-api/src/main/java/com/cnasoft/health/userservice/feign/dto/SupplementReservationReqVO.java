package com.cnasoft.health.userservice.feign.dto;

import com.cnasoft.health.common.util.io.serializer.DateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@ApiModel("补充预约请求VO")
public class SupplementReservationReqVO {

    public interface Create {
    }

    @ApiModelProperty(value = "0:待确认 1:已确认 2:已完成 3:已拒绝 4:已取消")
    private Integer status;

    @NotNull(groups = SupplementReservationReqVO.Create.class, message = "咨询师ID不能为空")
    @ApiModelProperty(value = "咨询师id", required = true)
    private Long psychiatristId;

    @ApiModelProperty(value = "预约人id", required = true)
    private Long userId;

    @ApiModelProperty(value = "用户角色类型 0:学生 1:家长 2:教职工")
    private Integer userRoleType;

    @NotNull(groups = SupplementReservationReqVO.Create.class, message = "预约日期不能为空")
    @ApiModelProperty(value = "预约日期", required = true)
    @JsonDeserialize(using = DateDeserializer.class)
    private Date date;

    @NotNull(groups = SupplementReservationReqVO.Create.class, message = "预约开始时间不能为空")
    @ApiModelProperty(value = "预约开始时间 ex: 0900", required = true)
    private String startTime;

    @NotNull(groups = SupplementReservationReqVO.Create.class, message = "预约结束时间不能为空")
    @ApiModelProperty(value = "预约结束时间 ex: 1000", required = true)
    private String endTime;

    @NotNull(groups = SupplementReservationReqVO.Create.class, message = "咨询问题不能为空")
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

    @NotNull(groups = SupplementReservationReqVO.Create.class, message = "咨询开始时间不能为空")
    @ApiModelProperty(value = "咨询开始时间", required = true)
    @JsonDeserialize(using = DateDeserializer.class)
    private Date consultationStartTime;

    @NotNull(groups = SupplementReservationReqVO.Create.class, message = "预约结束时间不能为空")
    @ApiModelProperty(value = "咨询结束时间", required = true)
    @JsonDeserialize(using = DateDeserializer.class)
    private Date consultationEndTime;

    @NotNull(groups = SupplementReservationReqVO.Create.class, message = "咨询类型不能为空")
    @ApiModelProperty(value = "咨询类型 0:在线 1:线下", required = true)
    private Integer consultMethod;

    @NotNull(groups = SupplementReservationReqVO.Create.class, message = "初步评估不能为空")
    @ApiModelProperty(value = "初步评估 数据字典查询码值", required = true)
    private String diagnosticResult;

    @NotNull(groups = SupplementReservationReqVO.Create.class, message = "是否预警不能为空")
    @ApiModelProperty(value = "是否预警 0 否 1 是", required = true)
    private Integer isEarlyWarning;

    @ApiModelProperty(value = "关注等级 0 一般关注 1 中度关注 2 高度关注")
    private Integer warningLevel;

    @ApiModelProperty(value = "干预结果 0 跟踪观察 1 解除关注 2 需要转介")
    private Integer consultResult;

    @ApiModelProperty(value = "转介说明")
    private String referralDescription;

    @NotNull(groups = SupplementReservationReqVO.Create.class, message = "咨询主题不能为空")
    @ApiModelProperty(value = "咨询主题", required = true)
    private String theme;

    @NotNull(groups = SupplementReservationReqVO.Create.class, message = "咨询过程不能为空")
    @ApiModelProperty(value = "咨询过程", required = true)
    private String pivots;

    @NotNull(groups = SupplementReservationReqVO.Create.class, message = "效果评估不能为空")
    @ApiModelProperty(value = "效果评估", required = true)
    private String resultEvaluation;

    @ApiModelProperty(value = "咨询反思")
    private String additionalExplain;
}
