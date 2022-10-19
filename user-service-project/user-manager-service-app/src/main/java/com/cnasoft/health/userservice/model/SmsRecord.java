package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 短信发送记录
 *
 * @author Administrator
 * @TableName sms_record
 */
@TableName(value = "sms_record")
@Data
@EqualsAndHashCode(callSuper = false)
public class SmsRecord extends SuperModel<SmsRecord> {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *
     */
    private Long userId;

    /**
     *
     */
    private String mobile;

    /**
     * 发送内容
     */
    private String content;

    /**
     * 0：未使用；1：已使用
     */
    private Integer used;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 1：家长绑定学生；2：忘记密码；3：登录验证码；4：修改手机
     */
    private Integer smsType;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
