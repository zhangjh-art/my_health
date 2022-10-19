package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @author: zjh
 * @created: 2022/7/18
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("new_reservation")
public class NewReservation extends SuperModel<NewReservation> {

    /**
     * 主键id
     */
    @TableId
    private Long id;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 咨询师ID
     */
    private Long psychiatristId;

    /**
     * 预约人ID
     */
    private Long userId;

    /**
     * 是否代预约
     */
    private Integer isSubstituted;

    /**
     * 是否学生取消预约
     */
    private Integer canceledByStudent;

    /**
     * 用户类型
     */
    private Integer userRoleType;

    /**
     * 预约时间
     */
    private Date date;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 咨询问题类型
     */
    private String consultTypes;

    /**
     * 咨询问题补充描述
     */
    private String consultDescription;

    /**
     * 问题描述
     */
    private String description;

    /**
     * 预约备注
     */
    private String remark;

    /**
     * 取消/拒绝其它理由
     */
    private String cancelOtherReason;

    /**
     * 问题1
     */
    private String question1;

    /**
     * 问题2
     */
    private String question2;

    /**
     * 问题3
     */
    private String question3;

    /**
     * 问题4
     */
    private String question4;

    /**
     * 问题5
     */
    private String question5;
}
