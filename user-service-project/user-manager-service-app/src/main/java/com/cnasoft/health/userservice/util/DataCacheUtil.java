package com.cnasoft.health.userservice.util;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cnasoft.health.common.dto.SchoolDTO;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.userservice.convert.SchoolConvert;
import com.cnasoft.health.userservice.mapper.SchoolMapper;
import com.cnasoft.health.userservice.mapper.SysRoleAuthorityMapper;
import com.cnasoft.health.userservice.mapper.SysRoleMapper;
import com.cnasoft.health.userservice.model.School;
import com.cnasoft.health.userservice.model.SysAuthority;
import com.cnasoft.health.userservice.model.SysRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据缓存工具类
 * 缓存了学校、角色权限数据
 *
 * @author ganghe
 * @date 2022/9/10 23:15
 **/
@Slf4j
@Component
public class DataCacheUtil {

    @Resource
    private SchoolMapper schoolMapper;
    @Resource
    private SysRoleMapper sysRoleMapper;
    @Resource
    private SysRoleAuthorityMapper sysRoleAuthorityMapper;

    /**
     * 学校数据
     */
    private static final Map<Long, SchoolDTO> SCHOOL_MAP = new HashMap<>(16);

    /**
     * 角色权限集合
     */
    private static final Map<String, List<SysAuthority>> ROLE_AUTHORITY_MAP = new HashMap<>(16);

    @PostConstruct
    private void init() {
        //查询审核通过的学校数据
        List<School> schoolList = schoolMapper.selectList(Wrappers.lambdaQuery(School.class).eq(School::getApproveStatus, ApproveStatus.APPROVED.getCode()));
        if (CollUtil.isNotEmpty(schoolList)) {
            for (School school : schoolList) {
                SCHOOL_MAP.put(school.getId(), SchoolConvert.INSTANCE.convert(school));
            }
        }

        //查询审核通过的角色数据
        LambdaQueryWrapper<SysRole> queryWrapper = Wrappers.lambdaQuery(SysRole.class);
        queryWrapper.eq(SysRole::getApproveStatus, ApproveStatus.APPROVED.getCode());
        queryWrapper.eq(SysRole::getEnabled, Boolean.TRUE);
        List<SysRole> roles = sysRoleMapper.selectList(queryWrapper);
        if (CollUtil.isNotEmpty(roles)) {
            Map<Long, String> roleIdCodeMap = roles.stream().collect(Collectors.toMap(SysRole::getId, SysRole::getCode, (key1, key2) -> key2));

            //查询所有权限数据
            List<SysAuthority> allAuthorities = sysRoleAuthorityMapper.findAllAuthorities(null);
            if (CollUtil.isNotEmpty(allAuthorities)) {
                for (SysAuthority authority : allAuthorities) {
                    Long roleId = authority.getRoleId();
                    if (roleIdCodeMap.containsKey(roleId)) {
                        String roleCode = roleIdCodeMap.get(roleId);
                        List<SysAuthority> authorities = new ArrayList<>();
                        if (ROLE_AUTHORITY_MAP.containsKey(roleCode)) {
                            authorities = ROLE_AUTHORITY_MAP.get(roleCode);
                        }

                        authorities.add(authority);
                        ROLE_AUTHORITY_MAP.put(roleCode, authorities);
                    }
                }
            }
        }
    }

    /**
     * 新增或更新学校缓存数据
     *
     * @param schoolId  学校id
     * @param schoolDTO 学校数据
     */
    public static void updateSchoolCache(Long schoolId, SchoolDTO schoolDTO) {
        SCHOOL_MAP.put(schoolId, schoolDTO);
    }

    /**
     * 移除学校缓存数据
     *
     * @param schoolId 学校id
     */
    public static void removeSchoolCache(Long schoolId) {
        SCHOOL_MAP.remove(schoolId);
    }

    /**
     * 获取学校缓存数据
     *
     * @return 学校数据
     */
    public static Map<Long, SchoolDTO> getSchoolMap() {
        return SCHOOL_MAP;
    }

    /**
     * 更新角色权限缓存数据
     *
     * @param roleCode       角色编码
     * @param sysAuthorities 角色权限数据
     */
    public static void updateAuthorityCache(String roleCode, List<SysAuthority> sysAuthorities) {
        ROLE_AUTHORITY_MAP.put(roleCode, sysAuthorities);
    }

    /**
     * 根据角色编码获取角色权限数据
     *
     * @param roleCode 角色编码
     * @return 角色权限数据
     */
    public static List<SysAuthority> getAuthoritiesByRoleCode(String roleCode) {
        return ROLE_AUTHORITY_MAP.get(roleCode);
    }
}
