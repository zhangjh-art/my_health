package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 区域心理教研员
 *
 * @author lgf
 * @date 2022/3/29
 */
@Data
@TableName("area_teacher")
@EqualsAndHashCode(callSuper = false)
public class AreaTeacher extends SuperModel<AreaTeacher> {

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
     * 部门
     */
    private String department;

    /**
     * 岗位
     */
    private String post;

    /**
     * 职称
     */
    private String title;

    /**
     * 专业
     */
    private String major;

    /**
     * 擅长
     */
    private String specialty;

    /**
     * 工号
     */
    private String jobNumber;

    /**
     * 区域code
     */
    private Integer areaCode;

    /**
     * 是否承接任务
     */
    private Boolean isAcceptTask;

    /**
     * 资质文件地址
     */
    private String certificationFile;

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
