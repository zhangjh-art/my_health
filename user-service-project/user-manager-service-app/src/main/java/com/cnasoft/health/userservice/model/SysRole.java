package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色
 *
 * @author ganghe
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_role")
public class SysRole extends SuperModel {
    @TableId
    private Long id;

    /**
     * 角色编码
     */
    private String code;

    /**
     * 角色名
     */
    private String name;

    /**
     * 是否内置（false非内置，true内置），内置的不能删除
     */
    private Boolean preset;

    /**
     * 启用状态：false禁用,true启用
     */
    private Boolean enabled;

    /**
     * 审核状态: 0待审核，1通过，2拒绝
     */
    private Integer approveStatus;

    /**
     * 用户ID
     */
    @TableField(exist = false)
    private Long userId;

    /**
     * 审核时的备注信息
     */
    @TableField(exist = false)
    private String remark;
}
