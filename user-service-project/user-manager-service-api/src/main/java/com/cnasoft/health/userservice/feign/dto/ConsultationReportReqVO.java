package com.cnasoft.health.userservice.feign.dto;

import com.cnasoft.health.common.util.io.serializer.DateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@ApiModel("咨询报告请求VO")
public class ConsultationReportReqVO {

    public interface Create {
    }

    public interface Update {
    }

    @NotNull(groups = ConsultationReportReqVO.Update.class, message = "id不能为空")
    private Long id;

    @NotNull(groups = ConsultationReportReqVO.Create.class, message = "预约id不能为空")
    @ApiModelProperty(value = "预约id", required = true)
    private Long reservationId;

    @NotNull(groups = ConsultationReportReqVO.Create.class, message = "咨询开始时间不能为空")
    @ApiModelProperty(value = "咨询开始时间", required = true)
    @JsonDeserialize(using = DateDeserializer.class)
    private Date startTime;

    @NotNull(groups = ConsultationReportReqVO.Create.class, message = "预约结束时间不能为空")
    @ApiModelProperty(value = "咨询结束时间", required = true)
    @JsonDeserialize(using = DateDeserializer.class)
    private Date endTime;

    @NotNull(groups = ConsultationReportReqVO.Create.class, message = "咨询类型不能为空")
    @ApiModelProperty(value = "咨询类型 0:在线 1:线下", required = true)
    private Integer consultMethod;

    @NotNull(groups = ConsultationReportReqVO.Create.class, message = "初步评估不能为空")
    @ApiModelProperty(value = "初步评估 数据字典查询码值", required = true)
    private String diagnosticResult;

    @NotNull(groups = ConsultationReportReqVO.Create.class, message = "是否预警不能为空")
    @ApiModelProperty(value = "是否预警 0 否 1 是", required = true)
    private Integer isEarlyWarning;

    @ApiModelProperty(value = "关注等级 0 一般关注 1 中度关注 2 高度关注")
    private Integer warningLevel;

    @ApiModelProperty(value = "干预结果 0 跟踪观察 1 解除关注 2 需要转介")
    private Integer consultResult;

    @ApiModelProperty(value = "转介说明")
    private String referralDescription;

    @NotNull(groups = ConsultationReportReqVO.Create.class, message = "咨询主题不能为空")
    @ApiModelProperty(value = "咨询主题", required = true)
    private String theme;

    @NotNull(groups = ConsultationReportReqVO.Create.class, message = "咨询过程不能为空")
    @ApiModelProperty(value = "咨询过程", required = true)
    private String pivots;

    @NotNull(groups = ConsultationReportReqVO.Create.class, message = "效果评估不能为空")
    @ApiModelProperty(value = "效果评估", required = true)
    private String resultEvaluation;

    @ApiModelProperty(value = "咨询反思")
    private String additionalExplain;
}
