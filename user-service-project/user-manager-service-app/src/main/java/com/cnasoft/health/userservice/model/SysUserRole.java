package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import com.cnasoft.health.userservice.util.RedisUtils;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author ganghe
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("sys_user_role")
public class SysUserRole extends SuperModel {
    @TableId
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 角色编码
     */
    private String roleCode;

    public SysUserRole(Long userId, Long roleId, String roleCode) {
        this.userId = userId;
        this.roleId = roleId;
        this.roleCode = roleCode;
    }
}
