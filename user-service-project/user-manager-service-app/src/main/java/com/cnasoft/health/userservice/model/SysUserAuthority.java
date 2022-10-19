package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 用户权限
 *
 * @author ganghe
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("sys_user_authority")
public class SysUserAuthority extends SuperModel {
    @TableId
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 权限ID
     */
    private Long authorityId;

    /**
     * 是否拥有权限 0: 否, 1: 是
     */
    private boolean isOwned;

    public SysUserAuthority(Long userId, Long authorityId, boolean isOwned) {
        this.userId = userId;
        this.authorityId = authorityId;
        this.isOwned = isOwned;
    }
}
