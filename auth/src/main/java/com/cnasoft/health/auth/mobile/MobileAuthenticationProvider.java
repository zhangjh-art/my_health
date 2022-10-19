package com.cnasoft.health.auth.mobile;

import cn.hutool.core.collection.CollUtil;
import com.cnasoft.health.auth.exception.RoleSelectionException;
import com.cnasoft.health.auth.service.CnaSoftUserDetailService;
import com.cnasoft.health.common.dto.LoginResult;
import com.cnasoft.health.common.dto.LoginUserDTO;
import com.cnasoft.health.common.dto.SysUserDTO;
import com.cnasoft.health.common.token.MobileAuthenticationToken;
import com.cnasoft.health.common.util.JsonUtils;
import com.google.common.collect.Lists;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.cnasoft.health.userservice.constant.UserErrorCodeConstants.USER_PASSWORD_ERROR;
import static com.cnasoft.health.userservice.constant.UserErrorCodeConstants.USER_SELECTION_ERROR;

/**
 * @author cnasoft
 * @date 2020/7/2 19:58
 */
@Setter
@Slf4j
public class MobileAuthenticationProvider implements AuthenticationProvider {
    private CnaSoftUserDetailService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) {
        MobileAuthenticationToken authenticationToken = (MobileAuthenticationToken) authentication;
        String mobile = (String) authenticationToken.getPrincipal();
        String captcha = (String) authenticationToken.getCredentials();
        Long choseUserId = ((MobileAuthenticationToken) authentication).getChoseUserId();
        List<SysUserDTO> users = userDetailsService.loadUserByMobile(mobile, captcha);
        if (CollUtil.isEmpty(users)) {
            // 未找到用户
            log.debug("合法用户未找到");
            throw new InternalAuthenticationServiceException(USER_PASSWORD_ERROR.getMessage());
        }

        // 过滤掉不合法的用户
        List<SysUserDTO> allowedUsers = new ArrayList<>();
        users.forEach(user -> {
            if (userDetailsService.checkUser(user)) {
                allowedUsers.add(user);
            }
        });
        users = allowedUsers;
        if (CollUtil.isEmpty(users)) {
            // 未找到用户
            log.debug("合法用户未找到");
            throw new InternalAuthenticationServiceException(USER_PASSWORD_ERROR.getMessage());
        }

        SysUserDTO detail = null;
        if (users.size() == 1) {
            // 如果只有一个用户直接登录
            detail = users.get(0);
            choseUserId = detail.getId();
        } else {
            if (Objects.isNull(choseUserId) || choseUserId < 1) {
                // 没有提供选择的用户id
                // 多个用户所有的用户账号列表，并要求其选择choseUserId重新调用登陆接口
                List<LoginUserDTO> loginUsers = Lists.newArrayList();
                users.forEach(user -> loginUsers.add(new LoginUserDTO(user.getId(), user.getPresetRoleCode(),
                        user.getAreaName(), user.getSchoolName())));

                log.debug("未指定选择角色");
                throw new RoleSelectionException(String.format(USER_SELECTION_ERROR.getMessage() + ":%s",
                        JsonUtils.writeValueAsString(new LoginResult(loginUsers.size(), loginUsers))));
            } else {
                // 提供了choseUserId则找出id一致的用户进行登陆
                for (SysUserDTO user : users) {
                    if (user.getId().equals(choseUserId)) {
                        detail = user;
                        break;
                    }
                }

                if (Objects.isNull(detail)) {
                    // 并未找到对应用户
                    log.debug("未找到对应账户id {} 的用户", choseUserId);
                    throw new BadCredentialsException(USER_PASSWORD_ERROR.getMessage());
                }
            }
        }

        // 更改短信验证码为已使用
        Boolean result = userDetailsService.updatesSmsRecordUsedStatus(mobile, captcha);
        if (Boolean.FALSE.equals(result)) {
            throw new BadCredentialsException(USER_PASSWORD_ERROR.getMessage());
        }

        return new MobileAuthenticationToken(detail, captcha, choseUserId, detail.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return MobileAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
