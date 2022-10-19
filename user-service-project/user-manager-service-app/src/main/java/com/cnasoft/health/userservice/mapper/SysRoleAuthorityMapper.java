package com.cnasoft.health.userservice.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.model.SysAuthority;
import com.cnasoft.health.userservice.model.SysRoleAuthority;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author cnasoft
 * @date 2020/8/13 10:53
 */
@DS(Constant.DATA_SOURCE_MYSQL)
public interface SysRoleAuthorityMapper extends SuperMapper<SysRoleAuthority> {
    /**
     * 新增角色权限
     *
     * @param roleId      角色ID
     * @param authorityId 权限ID
     * @return 受影响的行数
     */
    @Insert("insert into sys_role_menu(role_id, authority_id) values(#{roleId}, #{authorityId})")
    int save(@Param("roleId") Long roleId, @Param("authorityId") Long authorityId);

    /**
     * 根据角色ID查询权限编码列表
     *
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    Set<String> findAuthoritiesByRoleId(Long roleId);

    /**
     * 根据角色ID集合查询权限数据
     *
     * @param roleIds 角色ID列表
     * @return 权限列表
     */
    List<SysAuthority> findAuthoritiesByRoleIds(@Param("roleIds") Set<Long> roleIds);

    /**
     * 根据角色code集合、权限类型查询权限数据
     *
     * @param roleCodes 角色编码列表
     * @return 权限列表
     */
    List<SysAuthority> findAuthoritiesByRoleCodes(@Param("roleCodes") Set<String> roleCodes);

    /**
     * 查询所有角色的权限数据
     *
     * @param roleIds 角色列表
     * @return 权限列表
     */
    List<SysAuthority> findAllAuthorities(@Param("roleIds") Set<Long> roleIds);

    /**
     * 根据权限id查询所属角色id
     *
     * @param authorityId 权限id
     * @return 角色id列表
     */
    List<Long> findRoleIdByAuthorityId(Long authorityId);

    /**
     * 根据角色ID查询权限ID列表
     *
     * @param roleId 角色id
     * @return 权限ID列表
     */
    Set<Long> findAuthIdByRoleId(@Param("roleId") Long roleId);
}
