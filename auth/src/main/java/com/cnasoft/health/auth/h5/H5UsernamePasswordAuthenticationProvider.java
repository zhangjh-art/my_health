package com.cnasoft.health.auth.h5;

import cn.hutool.core.collection.CollUtil;
import com.cnasoft.health.auth.exception.RoleSelectionException;
import com.cnasoft.health.auth.service.CnaSoftUserDetailService;
import com.cnasoft.health.common.dto.LoginResult;
import com.cnasoft.health.common.dto.LoginUserDTO;
import com.cnasoft.health.common.dto.SysUserDTO;
import com.cnasoft.health.common.enums.RoleEnum;
import com.cnasoft.health.common.token.H5UsernamePasswordToken;
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

import static com.cnasoft.health.userservice.constant.UserErrorCodeConstants.USER_PASSWORD_ERROR;
import static com.cnasoft.health.userservice.constant.UserErrorCodeConstants.USER_SELECTION_ERROR;

/**
 * @author cnasoft
 * @date 2020/7/2 19:52
 */
@Slf4j
public class H5UsernamePasswordAuthenticationProvider implements AuthenticationProvider {

    private CnaSoftUserDetailService userDetailService;

    public void setUserDetailService(UserDetailsService userDetailsService) {
        userDetailService = (CnaSoftUserDetailService)userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        H5UsernamePasswordToken authenticationToken = (H5UsernamePasswordToken)authentication;
        String username = (String)authenticationToken.getPrincipal();
        Long choseUserId = authenticationToken.getChoseUserId();
        List<SysUserDTO> users = userDetailService.findAllUserInUsernameOrMobileOrShortId(username);
        if (CollUtil.isEmpty(users)) {
            throw new InternalAuthenticationServiceException(USER_PASSWORD_ERROR.getMessage());
        }

        // ???????????????????????????
        List<SysUserDTO> allowedUsers = new ArrayList<>();
        users.forEach(user -> {
            if (userDetailService.checkUser(user)) {
                allowedUsers.add(user);
            }
        });

        // h5?????????????????????????????????????????????
        allowedUsers.removeIf(user -> !(user.getPresetRoleCode().equals(RoleEnum.student.getValue()) || user.getPresetRoleCode().equals(RoleEnum.parents.getValue())));

        users = allowedUsers;
        if (CollUtil.isEmpty(users)) {
            // ???????????????
            log.debug("?????????????????????");
            throw new InternalAuthenticationServiceException(USER_PASSWORD_ERROR.getMessage());
        }

        if (Objects.isNull(authentication.getCredentials())) {
            // ???????????????
            log.debug("?????????????????????");
            throw new BadCredentialsException(USER_PASSWORD_ERROR.getMessage());
        }

        String password = authentication.getCredentials().toString();
        SysUserDTO detail = null;
        if (users.size() == 1) {
            // ????????????????????????????????????????????????
            detail = users.get(0);
            if (userDetailService.pwdCheck(detail, password)) {
                choseUserId = detail.getId();
            } else {
                // ????????????
                throw new BadCredentialsException(USER_PASSWORD_ERROR.getMessage());
            }
        } else {
            if (Objects.isNull(choseUserId) || choseUserId < 1) {
                // ????????????choseUserId
                // ????????????????????????????????????????????????????????????????????????????????????????????????userId????????????????????????
                List<LoginUserDTO> loginUsers = Lists.newArrayList();
                users.forEach(user -> {
                    if (userDetailService.pwdCheck(user, password)) {
                        loginUsers.add(new LoginUserDTO(user.getId(), user.getPresetRoleCode(), user.getAreaName(), user.getSchoolName()));
                    }
                });

                if (loginUsers.size() == 0) {
                    // ??????????????????????????????
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
                    log.debug("?????????????????????");
                    throw new RoleSelectionException(
                        String.format(USER_SELECTION_ERROR.getMessage() + ":%s", JsonUtils.writeValueAsString(new LoginResult(users.size(), loginUsers))));
                }
            } else {
                // ?????????choseUserId?????????id???????????????????????????
                for (SysUserDTO user : users) {
                    if (user.getId().equals(choseUserId)) {
                        if (!userDetailService.pwdCheck(user, password)) {
                            // ???????????????
                            log.debug("??????????????????????????????????????????");
                            throw new BadCredentialsException(USER_PASSWORD_ERROR.getMessage());
                        }
                        detail = user;
                        break;
                    }
                }

                if (Objects.isNull(detail)) {
                    // ????????????????????????
                    log.debug("?????????????????????id {} ?????????", choseUserId);
                    throw new BadCredentialsException(USER_PASSWORD_ERROR.getMessage());
                }
            }
        }

        return new H5UsernamePasswordToken(detail, authentication.getCredentials(), detail.getAuthorities(), choseUserId);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return H5UsernamePasswordToken.class.isAssignableFrom(authentication);
    }
}
