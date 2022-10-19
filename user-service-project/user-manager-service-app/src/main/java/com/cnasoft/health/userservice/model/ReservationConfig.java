package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;

/**
 * @author: zjh
 * @created: 2022/7/19
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("reservation_config")
public class ReservationConfig extends SuperModel<ReservationConfig> {

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
     * 是否自动确认
     */
    private Integer autoConfirm;

    /**
     * 过期日期
     */
    private Date expireDate;

    /**
     * 单次出诊时间
     */
    private Integer intervalTime;

    /**
     * 单次就诊人数
     */
    private Integer intervalNum;

    /**
     * 提前预约时间
     */
    private Integer advanceTime;
}
