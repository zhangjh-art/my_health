package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author ganghe
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_authority")
public class SysAuthority extends SuperModel<SysAuthority> {
    @TableId
    private Long id;

    /**
     * 权限类型：1目录2菜单3按钮
     */
    private Integer type;

    /**
     * 权限名
     */
    private String name;

    /**
     * 访问路径code
     */
    private String code;

    /**
     * 访问路径父级code
     */
    private String parentCode;

    /**
     * 请求接口:json
     */
    private String interfaces;

    /**
     * 备注
     */
    private String remark;

    /**
     * 启用状态：false禁用,true启用
     */
    private Boolean enabled;

    /**
     * 审核状态 0未审核 1审核通过 2已拒绝
     */
    private Integer approveStatus;

    @TableField(exist = false)
    private Boolean isOwned;

    @TableField(exist = false)
    private Long userId;

    @TableField(exist = false)
    private Long roleId;
}
