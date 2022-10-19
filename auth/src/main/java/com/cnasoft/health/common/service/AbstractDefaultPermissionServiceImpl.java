package com.cnasoft.health.common.service;

import cn.hutool.core.collection.CollUtil;
import com.cnasoft.health.common.properties.SecurityProperties;
import com.cnasoft.health.common.constant.CommonConstant;
import com.cnasoft.health.common.context.TenantContextHolder;
import com.cnasoft.health.common.dto.InterfaceVO;
import com.cnasoft.health.common.dto.SysAuthoritySimpleDTO;
import com.cnasoft.health.common.dto.SysUserDTO;
import com.cnasoft.health.common.util.AuthUtil;
import com.cnasoft.health.common.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 请求权限判断
 */
@Slf4j
public abstract class AbstractDefaultPermissionServiceImpl {
    @Autowired
    private SecurityProperties securityProperties;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    public boolean hasPermission(Authentication authentication, String requestMethod, String requestUri) {
        // 前端跨域OPTIONS请求预检放行 也可通过前端配置代理实现
        if (HttpMethod.OPTIONS.name().equalsIgnoreCase(requestMethod)) {
            return true;
        }

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            //判断是否开启url权限验证
            if (Boolean.FALSE.equals(securityProperties.getAuth().getUrlPermission().getEnable())) {
                return true;
            }

            //超级管理员super_admin无需认证
            String username = AuthUtil.getUserName(authentication);
            if (CommonConstant.SUPER_ADMIN_USER_NAME.equals(username)) {
                return true;
            }

            OAuth2Authentication auth2Authentication = (OAuth2Authentication) authentication;
            //判断应用黑白名单
            if (!isNeedAuth(auth2Authentication.getOAuth2Request().getClientId())) {
                return true;
            }

            //判断不进行url权限认证的api，所有已登录用户都能访问的url
            for (String path : securityProperties.getAuth().getUrlPermission().getIgnoreUrls()) {
                if (antPathMatcher.match(path, requestUri)) {
                    return true;
                }
            }

            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            if (CollUtil.isEmpty(authorities)) {
                log.warn("角色列表为空：{}", authentication.getPrincipal());
                return false;
            }

            //保存租户信息
            String clientId = auth2Authentication.getOAuth2Request().getClientId();
            TenantContextHolder.setTenant(clientId);

            Object principal = auth2Authentication.getUserAuthentication().getPrincipal();
            if (principal instanceof SysUserDTO) {
                //校验权限
                SysUserDTO user = (SysUserDTO) authentication.getPrincipal();
                Set<SysAuthoritySimpleDTO> permissions = user.getPermissions();
                if (permissions == null) {
                    return false;
                }

                Set<String> permissionList = new HashSet<>();
                for (SysAuthoritySimpleDTO permission : permissions) {
                    if (StringUtils.isNotBlank(permission.getInterfaces()) && !"[]".equals(permission.getInterfaces())) {
                        List<InterfaceVO> interfaces = JsonUtils.readValue(permission.getInterfaces(), new TypeReference<List<InterfaceVO>>() {
                        });

                        if (CollectionUtils.isEmpty(interfaces)) {
                            continue;
                        }

                        for (InterfaceVO anInterface : interfaces) {
                            if (StringUtils.isNotBlank(anInterface.getPath()) && StringUtils.isNotBlank(anInterface.getMethod())) {
                                permissionList.add(anInterface.getPath() + "##" + anInterface.getMethod());
                            }
                        }
                    }
                }

                if (CollUtil.isEmpty(permissionList)) {
                    return false;
                }

                String permissionKey = requestUri + "##" + requestMethod;
                return permissionList.contains(permissionKey);
            }
        }
        return false;
    }

    /**
     * 判断应用是否满足白名单和黑名单的过滤逻辑
     *
     * @param clientId 应用id
     * @return true(需要认证)，false(不需要认证)
     */
    private boolean isNeedAuth(String clientId) {
        boolean result = true;
        //白名单
        List<String> includeClientIds = securityProperties.getAuth().getUrlPermission().getIncludeClientIds();
        //黑名单
        List<String> exclusiveClientIds = securityProperties.getAuth().getUrlPermission().getExclusiveClientIds();
        if (!includeClientIds.isEmpty()) {
            if (includeClientIds.contains(clientId)) {
                result = false;
            }
        } else if (!exclusiveClientIds.isEmpty()) {
            result = !exclusiveClientIds.contains(clientId);
        }
        return result;
    }
}
