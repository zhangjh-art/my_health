package com.cnasoft.health.userservice.service.impl;

import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.userservice.mapper.SysUserAuthorityMapper;
import com.cnasoft.health.userservice.model.SysAuthority;
import com.cnasoft.health.userservice.model.SysUserAuthority;
import com.cnasoft.health.userservice.service.ISysUserAuthorityService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author ganghe
 */
@Service
public class SysUserAuthorityServiceImpl extends SuperServiceImpl<SysUserAuthorityMapper, SysUserAuthority> implements ISysUserAuthorityService {
    @Resource
    private SysUserAuthorityMapper userAuthorityMapper;

    @Override
    public List<SysAuthority> findAuthoritiesByUserId(Long userId) {
        return userAuthorityMapper.findAuthoritiesByUserId(userId);
    }

    @Override
    public List<SysAuthority> findAuthorities(List<Long> userIds) {
        return userAuthorityMapper.findAuthorities(userIds);
    }
}
