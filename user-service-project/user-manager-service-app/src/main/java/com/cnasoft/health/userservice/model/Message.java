package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Created by lgf on 2022/3/29.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("message")
public class Message extends SuperModel<Message> {
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
     * 消息类型
     */
    private Integer type;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息参数
     */
    private String params;

    /**
     * 是否已读
     */
    private boolean hasRead;
}
