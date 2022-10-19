package com.cnasoft.health.gateway.filter;

import cn.hutool.core.collection.CollUtil;
import com.cnasoft.health.common.constant.SecurityConstants;
import com.cnasoft.health.common.dto.SysUserDTO;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.FORM_BODY_WRAPPER_FILTER_ORDER;

/**
 * 通过header传递用户基础信息(userid, username等)到后端微服务
 */
@Slf4j
@Component
public class UserInfoHeaderFilter extends ZuulFilter {

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FORM_BODY_WRAPPER_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @SneakyThrows
    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        final HttpServletRequest request = ctx.getRequest();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        try {
            if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
                Object principal = authentication.getPrincipal();

                if (principal instanceof SysUserDTO) {
                    SysUserDTO user = (SysUserDTO)authentication.getPrincipal();
                    ctx.addZuulRequestHeader(SecurityConstants.USER_ID_HEADER, user.getIdStr());
                    ctx.addZuulRequestHeader(SecurityConstants.USER_NAME_HEADER, URLEncoder.encode(user.getName(), "UTF-8"));

                    Set<String> roleCodes = new HashSet<>();
                    if (StringUtils.isNotBlank(user.getPresetRoleCode())) {
                        roleCodes.add(user.getPresetRoleCode());
                    }
                    if (CollUtil.isNotEmpty(user.getCustomRoleCodes())) {
                        roleCodes.addAll(user.getCustomRoleCodes());
                    }

                    ctx.addZuulRequestHeader(SecurityConstants.ROLE_HEADER, CollUtil.join(roleCodes, ","));
                }

                OAuth2Authentication oauth2Authentication = (OAuth2Authentication)authentication;
                String clientId = oauth2Authentication.getOAuth2Request().getClientId();
                ctx.addZuulRequestHeader(SecurityConstants.TENANT_HEADER, clientId);

                // 加入防刷验证
                // try {
                //     if (duplicateCheckNeeded && requestDeDuplicator.isRequestDuplicate(request, authentication)) {
                //   abort(ctx);
                // }
                // } catch (Exception e) {
                //  e.printStackTrace();
                //  abort(ctx);
                //  }
            }
        } finally {
            SecurityContextHolder.getContext().setAuthentication(null);
            SecurityContextHolder.clearContext();
        }


        return null;

    }
}
