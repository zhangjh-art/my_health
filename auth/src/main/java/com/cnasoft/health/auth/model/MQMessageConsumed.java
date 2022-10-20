package com.cnasoft.health.auth.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("rocketmq_message_consumed")
public class MQMessageConsumed {
    @TableId
    private Long id;

    /**
     * 消息id
     */
    private String messageId;
}
