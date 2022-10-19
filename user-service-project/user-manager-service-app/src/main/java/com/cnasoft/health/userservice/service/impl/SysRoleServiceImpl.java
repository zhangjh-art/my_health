package com.cnasoft.health.userservice.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.SysAuthorityDTO;
import com.cnasoft.health.common.dto.SysRoleDTO;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.lock.IDistLock;
import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.common.util.time.DateUtil;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.convert.SysRoleConvert;
import com.cnasoft.health.userservice.feign.dto.SysRoleCreateReqVO;
import com.cnasoft.health.userservice.feign.dto.SysRoleUpdateReqVO;
import com.cnasoft.health.userservice.mapper.SysRoleMapper;
import com.cnasoft.health.userservice.mapper.SysUserMapper;
import com.cnasoft.health.userservice.model.SysAuthority;
import com.cnasoft.health.userservice.model.SysRole;
import com.cnasoft.health.userservice.model.SysRoleAuthority;
import com.cnasoft.health.userservice.model.SysUser;
import com.cnasoft.health.userservice.service.ISysAuthorityService;
import com.cnasoft.health.userservice.service.ISysRoleAuthorityService;
import com.cnasoft.health.userservice.service.ISysRoleService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.cnasoft.health.common.exception.constant.GlobalErrorCodeConstants.DATE_NOT_EXIST;

/**
 * @author ganghe
 */
@Service
public class SysRoleServiceImpl extends SuperServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    private static final String LOCK_KEY_ROLE_CODE = "role_code:";

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private ISysRoleAuthorityService roleAuthorityService;

    @Resource
    private ISysAuthorityService authorityService;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private IDistLock lock;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SysRole saveRole(SysRoleCreateReqVO createReqVO) throws Exception {
        SysRole role = SysRoleConvert.INSTANCE.convertVO(createReqVO);
        role.setPreset(false);
        String code = role.getCode();
        role.setApproveStatus(ApproveStatus.TO_BE_APPROVED.getCode());

        this.saveOrUpdateIdempotency(role, lock, LOCK_KEY_ROLE_CODE + code, new QueryWrapper<SysRole>().eq("code", code), "角色code已存在");

        //保存角色权限
        Set<String> authorityCodes = createReqVO.getAuthorities();
        if (CollectionUtils.isNotEmpty(authorityCodes)) {

            List<SysRoleAuthority> roleAuthorities = new ArrayList<>();
            for (String authorityCode : authorityCodes) {
                SysAuthority authority = authorityService.getOne(new LambdaQueryWrapper<SysAuthority>().eq(SysAuthority::getCode, authorityCode));
                if (Objects.isNull(authority)) {
                    continue;
                }
                roleAuthorities.add(new SysRoleAuthority(role.getId(), authority.getId()));
            }
            roleAuthorityService.saveBatch(roleAuthorities);
        }
        return role;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateRole(SysRoleUpdateReqVO updateReqVO) {

        SysRole role = baseMapper.selectById(updateReqVO.getId());
        Assert.notNull(role, DATE_NOT_EXIST.getMessage());

        role.setName(updateReqVO.getName());
        role.setApproveStatus(ApproveStatus.APPROVED.getCode());
        this.saveOrUpdate(role);
        if (CollUtil.isEmpty(updateReqVO.getAuthorities())) {
            return;
        }
        authorityService.updateRoleAuth(role, updateReqVO.getAuthorities());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<BatchOperationTipDTO> deleteRole(Set<Long> ids) {
        List<BatchOperationTipDTO> resultMap = new ArrayList<>();

        if (CollUtil.isEmpty(ids)) {
            return resultMap;
        }

        for (Long id : ids) {
            SysRole role = baseMapper.selectById(id);
            if (Objects.isNull(role)) {
                resultMap.add(new BatchOperationTipDTO(id, "无效id"));
                continue;
            }

            baseMapper.deleteById(id);
            roleAuthorityService.getBaseMapper().delete(new LambdaQueryWrapper<SysRoleAuthority>().eq(SysRoleAuthority::getRoleId, id));
            sysUserMapper.delete(new LambdaQueryWrapper<SysUser>().eq(SysUser::getRoleCode, role.getCode()));
        }
        return resultMap;
    }

    private void fillAuthorities(List<SysRoleDTO> roleList) {
        if (CollUtil.isEmpty(roleList)) {
            return;
        }

        Set<Long> roleIds = roleList.parallelStream().map(SysRoleDTO::getId).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(roleIds)) {

            List<SysAuthorityDTO> authoritiesByRoleIds = roleAuthorityService.findAuthoritiesByRoleIds(roleIds);
            if (CollectionUtils.isNotEmpty(authoritiesByRoleIds)) {

                for (SysRoleDTO role : roleList) {
                    Set<String> authorities =
                        authoritiesByRoleIds.stream().filter(r -> r.getRoleId().equals(role.getId())).collect(Collectors.toSet()).parallelStream().map(SysAuthorityDTO::getCode)
                            .collect(Collectors.toSet());
                    role.setAuthorities(authorities);
                }
            }
        }
    }

    @Override
    public PageResult<SysRoleDTO> findRoles(Map<String, Object> params) {
        final String createDateParam = "createDate";
        List<SysRoleDTO> roleList = new ArrayList<>();
        Page<SysRole> page = new Page<>(MapUtil.getInt(params, Constant.PAGE_NUM, 1), MapUtil.getInt(params, Constant.PAGE_SIZE, 10));
        if (params.containsKey(Constant.ENABLED)) {
            params.put(Constant.ENABLED, Boolean.valueOf(params.get(Constant.ENABLED).toString()));
        }

        if (params.containsKey(createDateParam)) {
            Long createDate = MapUtil.getLong(params, createDateParam);
            String createDateStr = DateUtil.secondToLocalDateTime(createDate).format(FORMAT);
            params.put(createDateParam, createDateStr);
        }

        List<SysRole> roles = baseMapper.findList(page, params);

        if (CollectionUtils.isNotEmpty(roles)) {
            roleList = SysRoleConvert.INSTANCE.convertList(roles);
            fillAuthorities(roleList);
        }

        return PageResult.<SysRoleDTO>builder().data(roleList).count(page.getTotal()).build();
    }

    @Override
    public List<SysRoleDTO> findAll() {
        List<SysRoleDTO> roleList = SysRoleConvert.INSTANCE.convertList(baseMapper.selectList(null));
        fillAuthorities(roleList);
        return roleList;
    }
}
