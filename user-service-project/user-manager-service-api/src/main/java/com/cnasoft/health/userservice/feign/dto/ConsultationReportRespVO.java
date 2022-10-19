package com.cnasoft.health.userservice.feign.dto;

import com.cnasoft.health.common.util.io.serializer.DateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import java.util.Date;

@Data
public class ConsultationReportRespVO {

    private Long id;

    private Long reservationId;

    private Integer consultMethod;

    @JsonSerialize(using = DateSerializer.class)
    private Date startTime;

    @JsonSerialize(using = DateSerializer.class)
    private Date endTime;

    private String diagnosticResult;

    private Integer isEarlyWarning;

    private Integer warningLevel;

    private Integer consultResult;

    private String theme;

    private String pivots;

    private String resultEvaluation;

    private String additionalExplain;

    private String referralDescription;
}
