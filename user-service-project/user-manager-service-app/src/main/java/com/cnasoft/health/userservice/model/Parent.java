package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 家长表
 *
 * @author zcb
 */
@TableName(value = "parent")
@Data
@EqualsAndHashCode(callSuper = false)
public class Parent extends SuperModel<Parent> {

    @TableId
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户姓名
     */
    private String name;

    /**
     * 性别: 1: 男，2: 女
     */
    private Integer sex;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 学校id
     */
    private Long schoolId;

    /**
     * 启用状态: 0: 否, 1: 是
     */
    private Boolean enabled;

    /**
     * 是否已激活,0: 否, 1: 是
     */
    private Boolean isActive;

    /**
     * 是否已确认,0: 否, 1: 是
     */
    private Boolean confirmed;

    @TableField(exist = false)
    private String key;
}