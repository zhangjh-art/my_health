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
@TableName("reservation_time_config")
public class ReservationTimeConfig extends SuperModel<ReservationTimeConfig> {

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
     * 预约配置id
     */
    private Long configId;

    /**
     * 周几
     */
    private Integer weekDay;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;
}
