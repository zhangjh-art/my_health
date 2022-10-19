package com.cnasoft.health.userservice.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.SysAuthorityDTO;
import com.cnasoft.health.common.dto.SysAuthoritySimpleDTO;
import com.cnasoft.health.common.dto.SysUserDTO;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.convert.SysAuthorityConvert;
import com.cnasoft.health.userservice.feign.dto.SysAuthorityReqVO;
import com.cnasoft.health.userservice.feign.dto.SysRoleAuthorityCreateVO;
import com.cnasoft.health.userservice.mapper.SysAuthorityMapper;
import com.cnasoft.health.userservice.model.SysAuthority;
import com.cnasoft.health.userservice.model.SysRole;
import com.cnasoft.health.userservice.model.SysRoleAuthority;
import com.cnasoft.health.userservice.service.ISysAuthorityService;
import com.cnasoft.health.userservice.service.ISysRoleAuthorityService;
import com.cnasoft.health.userservice.service.ISysRoleService;
import com.cnasoft.health.userservice.service.ISysUserService;
import com.cnasoft.health.userservice.util.DataCacheUtil;
import com.cnasoft.health.userservice.util.RedisUtils;
import com.cnasoft.health.userservice.util.UserUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.cnasoft.health.common.exception.util.ServiceExceptionUtil.exception;
import static com.cnasoft.health.userservice.constant.UserErrorCodeConstants.AUTHORITY_NOT_EXISTS;
import static com.cnasoft.health.userservice.constant.UserErrorCodeConstants.ROLE_NOT_EXISTS;

/**
 * @author ganghe
 */
@Service
public class SysAuthorityServiceImpl extends SuperServiceImpl<SysAuthorityMapper, SysAuthority> implements ISysAuthorityService {

    @Resource
    private ISysRoleAuthorityService roleAuthorityService;

    @Resource
    private ISysUserService userService;

    @Resource
    private ISysRoleService roleService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void setAuthorityToRole(SysRoleAuthorityCreateVO createVO) {
        String roleCode = createVO.getRoleCode();
        SysRole role = roleService.getOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, roleCode));
        Assert.notNull(role, ROLE_NOT_EXISTS.getMessage());
        Set<String> authorityCodes = createVO.getAuthorityCodes();
        if (CollUtil.isEmpty(authorityCodes)) {
            return;
        }
        updateRoleAuth(role, authorityCodes);
    }

    @Override
    public void updateRoleAuth(SysRole role, Set<String> authorityCodes) {
        Long roleId = role.getId();
        String roleCode = role.getCode();
        roleAuthorityService.remove(new LambdaQueryWrapper<SysRoleAuthority>().eq(SysRoleAuthority::getRoleId, roleId));

        List<SysRoleAuthority> roleAuthorities = new ArrayList<>();
        List<SysAuthority> authorities = new ArrayList<>();
        for (String authorityCode : authorityCodes) {
            SysAuthority authority = baseMapper.selectOne(new LambdaQueryWrapper<SysAuthority>().eq(SysAuthority::getCode, authorityCode));
            if (Objects.isNull(authority)) {
                continue;
            }

            authorities.add(authority);
            roleAuthorities.add(new SysRoleAuthority(roleId, authority.getId()));
        }
        roleAuthorityService.saveBatch(roleAuthorities);

        //给权限集合设置角色id
        authorities.forEach(authority -> authority.setRoleId(roleId));

        //更新该角色的权限缓存数据
        DataCacheUtil.updateAuthorityCache(roleCode, authorities);

        //更新有该角色的用户缓存信息
        List<Long> userIdList = userService.getUserIdListByRoleCode(roleCode);
        if (CollUtil.isEmpty(userIdList)) {
            return;
        }

        for (Long userId : userIdList) {
            SysUserDTO userDTO = RedisUtils.getUserByCache(userId);
            if (Objects.nonNull(userDTO)) {
                Set<SysAuthoritySimpleDTO> permissions = userDTO.getPermissions();
                if (CollectionUtils.isNotEmpty(permissions)) {
                    // 移除该角色所有的权限数据
                    permissions.removeIf(authorityDTO -> Objects.nonNull(authorityDTO.getRoleId()) && authorityDTO.getRoleId().equals(roleId));
                } else {
                    permissions = new HashSet<>();
                }

                // 加入当前角色的权限数据
                permissions.addAll(SysAuthorityConvert.INSTANCE.convertSimpleList(authorities));

                userDTO.setPermissions(permissions);
                userDTO.setPermissionCodes(permissions.stream().map(SysAuthoritySimpleDTO::getCode).collect(Collectors.toSet()));

                RedisUtils.cacheUser(userDTO);
            }
        }
    }

    /**
     * 角色权限列表
     *
     * @param roleIds 角色ids
     * @return 权限集合
     */
    @Override
    public List<SysAuthorityDTO> findByRoles(Set<Long> roleIds) {
        return roleAuthorityService.findAuthoritiesByRoleIds(roleIds);
    }

    @Override
    public List<SysAuthorityDTO> findByRoleCodes(Set<String> roleCodes) {
        return roleAuthorityService.findAuthoritiesByRoleCodes(roleCodes);
    }

    @Override
    public PageResult<SysAuthorityDTO> findList(Map<String, Object> params) {
        Page<SysAuthorityDTO> page = new Page<>(MapUtil.getInt(params, Constant.PAGE_NUM, 1), MapUtil.getInt(params, Constant.PAGE_SIZE, 10));
        UserUtil.setSearchParams(params);

        List<SysAuthorityDTO> pageList = baseMapper.findList(page, params);
        return PageResult.<SysAuthorityDTO>builder().data(pageList).count(page.getTotal()).build();
    }

    /**
     * 查询所有权限
     */
    @Override
    public List<SysAuthorityDTO> findAll() {
        LambdaQueryWrapper<SysAuthority> queryWrapper = new LambdaQueryWrapper<SysAuthority>().eq(SysAuthority::getEnabled, true);
        return SysAuthorityConvert.INSTANCE.convertList(baseMapper.selectList(queryWrapper));
    }

    @Override
    public void createAuthority(SysAuthorityReqVO reqVO) {
        SysAuthority authority = SysAuthorityConvert.INSTANCE.convertVO(reqVO);
        authority.setApproveStatus(ApproveStatus.APPROVED.getCode());

        baseMapper.insert(authority);
    }

    @Override
    public void updateAuthority(SysAuthorityReqVO reqVO) {
        SysAuthority oldAuthority = baseMapper.selectById(reqVO.getId());
        if (oldAuthority == null) {
            throw exception(AUTHORITY_NOT_EXISTS);
        }

        SysAuthority authority = SysAuthorityConvert.INSTANCE.convertVO(reqVO);
        authority.setCode(null);
        baseMapper.updateById(authority);
    }

    @Override
    public void updateEnabled(Long id, Boolean enabled) {
        SysAuthority authority = baseMapper.selectOne(new LambdaQueryWrapper<SysAuthority>().eq(SysAuthority::getId, id));
        Assert.notNull(authority, AUTHORITY_NOT_EXISTS.getMessage());

        authority.setEnabled(enabled);
        baseMapper.updateById(authority);
    }

    @Override
    public List<BatchOperationTipDTO> delete(Set<Long> ids) {
        List<BatchOperationTipDTO> resultMap = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(ids)) {
            for (Long id : ids) {
                SysAuthority authority = baseMapper.selectById(id);
                if (Objects.isNull(authority)) {
                    resultMap.add(new BatchOperationTipDTO(id, "无效id"));
                    continue;
                }

                baseMapper.deleteById(id);
            }
        }
        return resultMap;
    }
}
