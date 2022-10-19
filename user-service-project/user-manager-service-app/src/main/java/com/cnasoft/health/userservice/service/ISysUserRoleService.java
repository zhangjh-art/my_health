package com.cnasoft.health.userservice.service;

import com.cnasoft.health.common.service.ISuperService;
import com.cnasoft.health.userservice.model.SysRole;
import com.cnasoft.health.userservice.model.SysUserRole;

import java.util.List;
import java.util.Set;

/**
 * @author ganghe
 */
public interface ISysUserRoleService extends ISuperService<SysUserRole> {

    /**
     * 根据用户id获取自定义角色
     *
     * @param userId 用户ID
     * @return 用户集合
     */
    List<SysRole> findRolesByUserId(Long userId);

    /**
     * 批量更新用户角色
     *
     * @param sysUserRoles 用户集合
     * @return 行数
     */
    int insertBatch(List<SysUserRole> sysUserRoles);
}
