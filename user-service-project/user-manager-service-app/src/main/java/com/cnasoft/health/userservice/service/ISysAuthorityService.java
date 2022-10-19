package com.cnasoft.health.userservice.service;

import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.SysAuthorityDTO;
import com.cnasoft.health.common.service.ISuperService;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.feign.dto.SysAuthorityReqVO;
import com.cnasoft.health.userservice.feign.dto.SysRoleAuthorityCreateVO;
import com.cnasoft.health.userservice.model.SysAuthority;
import com.cnasoft.health.userservice.model.SysRole;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author ganghe
 */
public interface ISysAuthorityService extends ISuperService<SysAuthority> {

    /**
     * 权限列表
     *
     * @param params 请求参数
     * @return 分页数据
     */
    PageResult<SysAuthorityDTO> findList(Map<String, Object> params);

    /**
     * 查询所有权限
     *
     * @return 权限列表
     */
    List<SysAuthorityDTO> findAll();

    /**
     * 角色分配权限
     *
     * @param createVO 请求参数
     */
    void setAuthorityToRole(SysRoleAuthorityCreateVO createVO);

    /**
     * 更新角色权限
     * @param role 角色信息
     * @param authorityCodes 权限信息
     */
    void updateRoleAuth(SysRole role, Set<String> authorityCodes);
    /**
     * 根据角色ID获取权限列表
     *
     * @param roleIds 角色ids
     * @return 权限列表
     */
    List<SysAuthorityDTO> findByRoles(Set<Long> roleIds);

    /**
     * 角色权限列表
     *
     * @param roleCodes 角色编码
     * @return 权限列表
     */
    List<SysAuthorityDTO> findByRoleCodes(Set<String> roleCodes);

    /**
     * 新增权限
     *
     * @param reqVO 请求数据
     */
    void createAuthority(SysAuthorityReqVO reqVO);

    /**
     * 修改权限信息
     *
     * @param reqVO 请求数据
     */
    void updateAuthority(SysAuthorityReqVO reqVO);

    /**
     * 更新权限状态
     *
     * @param id      权限id
     * @param enabled 启用/禁用
     */
    void updateEnabled(Long id, Boolean enabled);

    /**
     * 删除权限
     *
     * @param ids 权限id列表
     * @return 受影响的行数
     */
    List<BatchOperationTipDTO> delete(Set<Long> ids);
}
