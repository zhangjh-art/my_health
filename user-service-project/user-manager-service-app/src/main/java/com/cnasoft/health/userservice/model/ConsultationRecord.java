package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @author: zjh
 * @created: 2022/7/27
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("consultation_record")
public class ConsultationRecord extends SuperModel<ConsultationRecord> {

    /**
     * 主键id
     */
    @TableId
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 咨询时间
     */
    private Date date;

    /**
     * 咨询方式
     */
    private Integer source;

    /**
     * 咨询问题ID
     */
    private Long sourceId;

    /**
     * 咨询问题类型
     */
    private String consultTypes;
}
