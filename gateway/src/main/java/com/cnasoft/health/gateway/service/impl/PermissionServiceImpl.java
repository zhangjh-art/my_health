package com.cnasoft.health.gateway.service.impl;

import com.cnasoft.health.common.service.AbstractDefaultPermissionServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * @author ganghe
 */
@Slf4j
@Service("permissionService")
public class PermissionServiceImpl extends AbstractDefaultPermissionServiceImpl {

    public boolean hasPermission(HttpServletRequest request, Authentication authentication) {
        return hasPermission(authentication, request.getMethod(), request.getRequestURI());
    }
}
