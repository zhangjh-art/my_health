package com.cnasoft.health.userservice.service.impl;

import com.cnasoft.health.common.dto.SysAuthorityDTO;
import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.userservice.convert.SysAuthorityConvert;
import com.cnasoft.health.userservice.mapper.SysRoleAuthorityMapper;
import com.cnasoft.health.userservice.model.SysRoleAuthority;
import com.cnasoft.health.userservice.service.ISysRoleAuthorityService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * 角色权限
 *
 * @author ganghe
 */
@Service
public class SysRoleAuthorityServiceImpl extends SuperServiceImpl<SysRoleAuthorityMapper, SysRoleAuthority> implements ISysRoleAuthorityService {
    @Resource
    private SysRoleAuthorityMapper sysRoleAuthorityMapper;

    @Override
    public int save(Long roleId, Long authorityId) {
        return sysRoleAuthorityMapper.save(roleId, authorityId);
    }

    @Override
    public Set<String> findAuthoritiesByRoleId(Long roleId) {
        return sysRoleAuthorityMapper.findAuthoritiesByRoleId(roleId);
    }

    @Override
    public Set<Long> findAuthIdByRoleId(Long roleId) {
        return sysRoleAuthorityMapper.findAuthIdByRoleId(roleId);
    }

    @Override
    public List<SysAuthorityDTO> findAuthoritiesByRoleIds(Set<Long> roleIds) {
        return SysAuthorityConvert.INSTANCE.convertList(sysRoleAuthorityMapper.findAuthoritiesByRoleIds(roleIds));
    }

    @Override
    public List<SysAuthorityDTO> findAuthoritiesByRoleCodes(Set<String> roleCodes) {
        return SysAuthorityConvert.INSTANCE.convertList(sysRoleAuthorityMapper.findAuthoritiesByRoleCodes(roleCodes));
    }

    @Override
    public List<Long> findRoleIdByAuthorityId(Long authorityId) {
        return sysRoleAuthorityMapper.findRoleIdByAuthorityId(authorityId);
    }
}
