package com.cnasoft.health.userservice.service;

import com.cnasoft.health.common.dto.SysAuthorityDTO;
import com.cnasoft.health.common.service.ISuperService;
import com.cnasoft.health.userservice.model.SysRoleAuthority;

import java.util.List;
import java.util.Set;

/**
 * 角色权限
 *
 * @author ganghe
 */
public interface ISysRoleAuthorityService extends ISuperService<SysRoleAuthority> {

    /**
     * 新增角色权限
     *
     * @param roleId
     * @param authorityId
     * @return
     */
    int save(Long roleId, Long authorityId);

    /**
     * 根据角色ID查询权限Code列表
     *
     * @param roleId
     * @return
     */
    Set<String> findAuthoritiesByRoleId(Long roleId);

    /**
     * 根据角色ID查询权限Id列表
     *
     * @param roleId
     * @return
     */
    Set<Long> findAuthIdByRoleId(Long roleId);

    /**
     * 根据角色id集合查询权限列表
     *
     * @param roleIds
     * @return
     */
    List<SysAuthorityDTO> findAuthoritiesByRoleIds(Set<Long> roleIds);

    /**
     * 根据角色code集合和权限类型查询权限列表
     *
     * @param roleCodes
     * @return
     */
    List<SysAuthorityDTO> findAuthoritiesByRoleCodes(Set<String> roleCodes);

    /**
     * 根据权限id查询所属角色id
     *
     * @param authorityId 权限id
     * @return 角色id列表
     */
    List<Long> findRoleIdByAuthorityId(Long authorityId);
}
