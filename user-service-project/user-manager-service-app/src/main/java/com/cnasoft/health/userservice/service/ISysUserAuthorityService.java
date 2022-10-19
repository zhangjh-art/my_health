package com.cnasoft.health.userservice.service;

import com.cnasoft.health.common.service.ISuperService;
import com.cnasoft.health.userservice.model.SysAuthority;
import com.cnasoft.health.userservice.model.SysUserAuthority;

import java.util.List;

/**
 * @author ganghe
 */
public interface ISysUserAuthorityService extends ISuperService<SysUserAuthority> {

    /**
     * 根据用户id获取权限
     *
     * @param userId
     * @return
     */
    List<SysAuthority> findAuthoritiesByUserId(Long userId);

    /**
     * 查询指定用户的用户权限数据
     *
     * @param userIds
     * @return
     */
    List<SysAuthority> findAuthorities(List<Long> userIds);
}
