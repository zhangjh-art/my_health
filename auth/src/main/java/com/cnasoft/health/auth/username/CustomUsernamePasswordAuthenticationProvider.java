package com.cnasoft.health.auth.username;

import cn.hutool.core.collection.CollUtil;
import com.cnasoft.health.auth.exception.RoleSelectionException;
import com.cnasoft.health.auth.service.CnaSoftUserDetailService;
import com.cnasoft.health.common.dto.LoginResult;
import com.cnasoft.health.common.dto.LoginUserDTO;
import com.cnasoft.health.common.dto.SysUserDTO;
import com.cnasoft.health.common.token.CustomUsernamePasswordAuthenticationToken;
import com.cnasoft.health.common.util.JsonUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.cnasoft.health.user.constant.UserErrorCodeConstants.USER_PASSWORD_ERROR;
import static com.cnasoft.health.user.constant.UserErrorCodeConstants.USER_SELECTION_ERROR;

/**
 * @author cnasoft
 * @date 2020/7/2 19:52
 */
@Slf4j
public class CustomUsernamePasswordAuthenticationProvider implements AuthenticationProvider {

    private CnaSoftUserDetailService userDetailService;

    public void setUserDetailService(UserDetailsService userDetailsService) {
        userDetailService = (CnaSoftUserDetailService)userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        CustomUsernamePasswordAuthenticationToken authenticationToken = (CustomUsernamePasswordAuthenticationToken)authentication;
        String username = (String)authenticationToken.getPrincipal();
        Long choseUserId = authenticationToken.getChoseUserId();

        List<SysUserDTO> users = userDetailService.findAllUserInUsernameOrMobileOrShortId(username);

        if (CollUtil.isEmpty(users)) {
            throw new InternalAuthenticationServiceException(USER_PASSWORD_ERROR.getMessage());
        }

        // 过滤掉不合法的用户
        List<SysUserDTO> allowedUsers = new ArrayList<>();
        users.forEach(user -> {
            if (userDetailService.checkUser(user)) {
                allowedUsers.add(user);
            }
        });
        users = allowedUsers;
        if (CollUtil.isEmpty(users)) {
            // 未找到用户
            log.debug("合法用户未找到");
            throw new InternalAuthenticationServiceException(USER_PASSWORD_ERROR.getMessage());
        }

        if (Objects.isNull(authentication.getCredentials())) {
            // 未填写密码
            log.debug("未填写登陆密码");
            throw new BadCredentialsException(USER_PASSWORD_ERROR.getMessage());
        }

        String password = authentication.getCredentials().toString();
        SysUserDTO detail = null;
        if (users.size() == 1) {
            // 如果只有一个用户直接校验密码登录
            detail = users.get(0);
            if (userDetailService.pwdCheck(detail, password)) {
                choseUserId = detail.getId();
            } else {
                // 密码错误
                throw new BadCredentialsException(USER_PASSWORD_ERROR.getMessage());
            }
        } else {
            if (Objects.isNull(choseUserId) || choseUserId < 1) {
                // 没有提供choseUserId
                // 多个用户返回匹配密码的用户列表以及合法的总用户数量，并要求其指定userId重新调用登陆接口
                List<LoginUserDTO> loginUsers = Lists.newArrayList();
                users.forEach(user -> {
                    if (userDetailService.pwdCheck(user, password)) {
                        loginUsers.add(new LoginUserDTO(user.getId(), user.getPresetRoleCode(), user.getAreaName(), user.getSchoolName()));
                    }
                });

                if (loginUsers.size() == 0) {
                    // 没有匹配上密码的账户
                    throw new BadCredentialsException(USER_PASSWORD_ERROR.getMessage());
                }
                if (loginUsers.size() == 1) {
                    choseUserId = loginUsers.get(0).getUserId();
                    for (SysUserDTO user : users) {
                        if (user.getId().equals(choseUserId)) {
                            detail = user;
                            break;
                        }
                    }
                } else {
                    log.debug("未指定选择角色");
                    throw new RoleSelectionException(
                        String.format(USER_SELECTION_ERROR.getMessage() + ":%s", JsonUtils.writeValueAsString(new LoginResult(users.size(), loginUsers))));
                }
            } else {
                // 提供了choseUserId则找出id一致的用户进行登陆
                for (SysUserDTO user : users) {
                    if (user.getId().equals(choseUserId)) {
                        if (!userDetailService.pwdCheck(user, password)) {
                            // 密码不正确
                            log.debug("指定角色对应账号的密码不正确");
                            throw new BadCredentialsException(USER_PASSWORD_ERROR.getMessage());
                        }
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

        return new CustomUsernamePasswordAuthenticationToken(detail, authentication.getCredentials(), detail.getAuthorities(), choseUserId);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return CustomUsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
