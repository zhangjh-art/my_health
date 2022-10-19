package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 校教职工
 *
 * @author lgf
 */
@Data
@TableName("school_staff")
@EqualsAndHashCode(callSuper = false)
public class SchoolStaff extends SuperModel<SchoolStaff> {
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
     * 工号
     */
    private String jobNumber;

    /**
     * 学校id
     */
    private Long schoolId;

    /**
     * 部门 数据字典值
     */
    private String department;

    /**
     * 岗位
     */
    private String post;

    /**
     * 教职工类型，1：教职工；2：班主任；3：领导
     */
    private Integer type;

    /**
     * 用户名
     */
    @TableField(exist = false)
    private String username;

    /**
     * 姓名
     */
    @TableField(exist = false)
    private String name;

    /**
     * 性别
     */
    @TableField(exist = false)
    private Integer sex;

    /**
     * 手机号
     */
    @TableField(exist = false)
    private String mobile;

    /**
     * 邮箱
     */
    @TableField(exist = false)
    private String email;

    /**
     * 启用/禁用
     */
    @TableField(exist = false)
    private Boolean enabled;

    /**
     * 头像
     */
    @TableField(exist = false)
    private String headImgUrl;

    /**
     * 昵称
     */
    @TableField(exist = false)
    private String nickname;
}
