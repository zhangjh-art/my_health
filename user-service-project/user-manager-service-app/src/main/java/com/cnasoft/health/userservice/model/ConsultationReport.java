package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @author: zjh
 * @created: 2022/7/22
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("consultation_report")
public class ConsultationReport extends SuperModel<ConsultationReport> {

    /**
     * 主键id
     */
    @TableId
    private Long id;

    /**
     * 预约id
     */
    private Long reservationId;

    /**
     * 咨询开始时间
     */
    private Date startTime;

    /**
     * 咨询结束时间
     */
    private Date endTime;

    /**
     * 咨询类型
     */
    private Integer consultMethod;

    /**
     * 初步评估
     */
    private String diagnosticResult;

    /**
     * 是否预警
     */
    private Integer isEarlyWarning;

    /**
     * 关注等级
     */
    private Integer warningLevel;

    /**
     * 干预结果
     */
    private Integer consultResult;

    /**
     * 咨询主题
     */
    private String theme;

    /**
     * 咨询过程
     */
    private String pivots;

    /**
     * 效果评估
     */
    private String resultEvaluation;

    /**
     * 咨询反思
     */
    private String additionalExplain;

    /**
     * 转介说明
     */
    private String referralDescription;
}
