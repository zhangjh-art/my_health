package com.cnasoft.health.userservice.service.impl;

import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.userservice.mapper.SysUserRoleMapper;
import com.cnasoft.health.userservice.model.SysRole;
import com.cnasoft.health.userservice.model.SysUserRole;
import com.cnasoft.health.userservice.service.ISysUserRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author ganghe
 */
@Slf4j
@Service
public class SysUserRoleServiceImpl extends SuperServiceImpl<SysUserRoleMapper, SysUserRole> implements ISysUserRoleService {
    @Resource
    private SysUserRoleMapper sysUserRoleMapper;

    @Override
    public List<SysRole> findRolesByUserId(Long userId) {
        return sysUserRoleMapper.findRolesByUserId(userId);
    }

    @Override
    public int insertBatch(List<SysUserRole> sysUserRoles) {
        return sysUserRoleMapper.insertBatch(sysUserRoles);
    }
}
