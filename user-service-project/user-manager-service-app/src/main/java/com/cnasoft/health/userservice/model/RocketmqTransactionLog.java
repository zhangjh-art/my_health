package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 这个表记录mq事务log，用于业务完成后，未发送消息，服务宕机，之后重启服务时，mq通过查询该表，重新发送消息
 * 该表mq不会有操作，完全自定义读写
 */
@TableName("rocketmq_transaction_log")
@Data
@EqualsAndHashCode(callSuper = false)
public class RocketmqTransactionLog {
    /**
     * 主键id
     */
    @TableId
    private Long id;

    private String transactionId;

    private String log;
}
