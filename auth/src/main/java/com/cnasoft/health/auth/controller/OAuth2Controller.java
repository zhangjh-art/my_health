package com.cnasoft.health.auth.controller;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.cnasoft.health.auth.service.CnaSoftUserDetailService;
import com.cnasoft.health.common.constant.SecurityConstants;
import com.cnasoft.health.common.context.TenantContextHolder;
import com.cnasoft.health.common.dto.LoginResult;
import com.cnasoft.health.common.dto.SysUserDTO;
import com.cnasoft.health.common.enums.RoleEnum;
import com.cnasoft.health.common.token.CustomUsernamePasswordAuthenticationToken;
import com.cnasoft.health.common.token.H5MobileToken;
import com.cnasoft.health.common.token.H5UsernamePasswordToken;
import com.cnasoft.health.common.token.MobileAuthenticationToken;
import com.cnasoft.health.common.token.PadAuthenticationToken;
import com.cnasoft.health.common.token.PadUsernamePasswordToken;
import com.cnasoft.health.common.util.AuthUtil;
import com.cnasoft.health.common.util.JsonUtils;
import com.cnasoft.health.common.util.ResponseUtil;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.userservice.feign.UserFeignClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.exceptions.UnapprovedClientAuthenticationException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

import static com.cnasoft.health.common.exception.constant.GlobalErrorCodeConstants.AVAILABLE_SERVICE;
import static com.cnasoft.health.userservice.constant.UserErrorCodeConstants.USER_DISABLED;
import static com.cnasoft.health.userservice.constant.UserErrorCodeConstants.USER_PASSWORD_ERROR;
import static com.cnasoft.health.userservice.constant.UserErrorCodeConstants.USER_SELECTION_ERROR;

/**
 * @author cnasoft
 * @date 2020/6/30 16:29
 */
@Slf4j
@RestController
@Api(tags = "认证服务API")
public class OAuth2Controller {
    //private static final Logger monitorLog = LoggerFactory.getLogger("com.cnasoft.health.log.monitor");

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private AuthorizationServerTokenServices authorizationServerTokenServices;

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private ClientDetailsService clientDetailsService;

    @Resource
    private CnaSoftUserDetailService userDetailsService;

    @Resource
    private TokenStore tokenStore;

    @ApiOperation(value = "将新添加的用户名及手机号放入过滤器中-b端")
    @PostMapping(SecurityConstants.NEW_USER_FILTER_CACHE)
    public void addUserToFilter(@RequestBody Set<String> usernameAndMobileList) {
        userDetailsService.addUserToFilter(usernameAndMobileList);
    }

    @ApiOperation(value = "根据用户名密码及通用验证码获取token")
    @PostMapping(SecurityConstants.PASSWORD_LOGIN_COMMON_CAPTCHA_URL)
    public void getUserTokenInfo(@ApiParam(required = true, name = "username", value = "账号") String username,
        @ApiParam(required = true, name = "password", value = "密码") String password, @ApiParam(name = "validCode", value = "验证码") String validCode,
        @ApiParam(name = "deviceId", value = "设备Id【获取验证码的设备id】") String deviceId, @ApiParam(name = "choseUserId", value = "用户选择的用户id") Long choseUserId, HttpServletRequest request,
        HttpServletResponse response) throws Exception {
        Date interfaceStartTime = new Date();
        CustomUsernamePasswordAuthenticationToken
            token = new CustomUsernamePasswordAuthenticationToken(username, password, choseUserId);
        writerToken(request, response, token, interfaceStartTime);
    }

    @ApiOperation(value = "根据手机号和验证码登录(获取token)")
    @PostMapping(SecurityConstants.MOBILE_TOKEN_URL)
    public void getTokenByMobile(@ApiParam(required = true, name = "mobile", value = "手机号") String mobile,
        @ApiParam(required = true, name = "captcha", value = "验证码") String captcha, @ApiParam(name = "choseUserId", value = "用户选择的用户id") Long choseUserId,
        HttpServletRequest request, HttpServletResponse response) throws Exception {
        Date interfaceStartTime = new Date();
        MobileAuthenticationToken token = new MobileAuthenticationToken(mobile, captcha, choseUserId);
        writerToken(request, response, token, interfaceStartTime);
    }

    @ApiOperation(value = "h5端根据手机号和验证码登录(获取token)")
    @PostMapping(SecurityConstants.H5_MOBILE_TOKEN_URL)
    public void getTokenByMobileH5(@ApiParam(required = true, name = "mobile", value = "手机号") String mobile,
        @ApiParam(required = true, name = "captcha", value = "验证码") String captcha, @ApiParam(name = "choseUserId", value = "用户选择的用户id") Long choseUserId,
        HttpServletRequest request, HttpServletResponse response) throws Exception {
        Date interfaceStartTime = new Date();
        H5MobileToken token = new H5MobileToken(mobile, captcha, choseUserId);
        writerToken(request, response, token, interfaceStartTime);
    }

    @ApiOperation(value = "h5端根据用户名密码及通用验证码获取token")
    @PostMapping(SecurityConstants.H5_PASSWORD_TOKEN_URL)
    public void getTokenByPWDH5(@ApiParam(required = true, name = "username", value = "账号") String username,
        @ApiParam(required = true, name = "password", value = "密码") String password, @ApiParam(name = "choseUserId", value = "用户选择的用户id") Long choseUserId,
        HttpServletRequest request, HttpServletResponse response) throws Exception {
        Date interfaceStartTime = new Date();
        H5UsernamePasswordToken token = new H5UsernamePasswordToken(username, password, choseUserId);
        writerToken(request, response, token, interfaceStartTime);
    }

    @ApiOperation(value = "PAD端根据用户ID获取token(学生扫码登录)")
    @PostMapping(SecurityConstants.PAD_USER_ID_TOKEN_URL)
    public void getTokenByUserId(@ApiParam(required = true, name = "userId", value = "用户id") Long userId, HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        Date interfaceStartTime = new Date();
        PadAuthenticationToken token = new PadAuthenticationToken(userId);
        writerToken(request, response, token, interfaceStartTime);
    }

    @ApiOperation(value = "PAD端根据用户名和密码获取token(测试管理员登录)")
    @PostMapping(SecurityConstants.PAD_PASSWORD_TOKEN_URL)
    public void getTokenByPWDPAD(@ApiParam(required = true, name = "username", value = "账号") String username,
        @ApiParam(required = true, name = "password", value = "密码") String password, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Date interfaceStartTime = new Date();
        PadUsernamePasswordToken token = new PadUsernamePasswordToken(username, password);
        writerToken(request, response, token, interfaceStartTime);
    }

    //    @ApiOperation(value = "根据用户名密码及滑块验证码验证结果获取token")
    //    @PostMapping(SecurityConstants.PASSWORD_LOGIN_SLIDE_CAPTCHA_URL)
    //    public void getTokenByUserAndSliderCaptcha(
    //            @ApiParam(required = true, name = "username", value = "账号") String username,
    //            @ApiParam(required = true, name = "password", value = "密码") String password,
    //            @ApiParam(required = true, name = "captchaKey", value = "滑块验证码验证通过生成的KEY") String captchaKey,
    //            HttpServletRequest request, HttpServletResponse response) throws IOException {
    //        String cacheKey = redisRepository.get(RedisDataSource.SLIDER_CAPTCHA_ACCOUNT + username);
    //        if (cacheKey.equals(captchaKey)) {
    //            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
    //            writerToken(request, response, token);
    //        } else {
    //            response.setStatus(GlobalErrorCodeConstants.SLIDER_CAPTCHA_CHECK_ERROR.getCode());
    //            ResponseUtil.responseUnAuthorized(response, "验证失败");
    //        }
    //    }

    //    @ApiOperation(value = "openId获取token")
    //    @PostMapping(SecurityConstants.OPENID_TOKEN_URL)
    //    public void getTokenByOpenId(
    //            @ApiParam(required = true, name = "openId", value = "openId") String openId,
    //            HttpServletRequest request, HttpServletResponse response) throws IOException {
    //        OpenIdAuthenticationToken token = new OpenIdAuthenticationToken(openId);
    //        writerToken(request, response, token);
    //    }

    private void writerToken(HttpServletRequest request, HttpServletResponse response, AbstractAuthenticationToken token, Date interfaceStartTime) throws Exception {
        try {
            final String[] clientInfos = AuthUtil.extractHeaderClient(request);
            String clientId = clientInfos[0];
            String clientSecret = clientInfos[1];
            ClientDetails clientDetails = getClient(clientId, clientSecret);
            //保存租户id
            TenantContextHolder.setTenant(clientId);
            TokenRequest tokenRequest = new TokenRequest(MapUtil.empty(), clientId, clientDetails.getScope(), "customer");
            OAuth2Request oAuth2Request = tokenRequest.createOAuth2Request(clientDetails);
            Authentication authentication = authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(oAuth2Request, authentication);
            OAuth2AccessToken oAuth2AccessToken = authorizationServerTokenServices.createAccessToken(oAuth2Authentication);
            oAuth2Authentication.setAuthenticated(true);
            TenantContextHolder.clear();

            if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof SysUserDTO) {
                    SysUserDTO user = (SysUserDTO)authentication.getPrincipal();
                    String roleCode = user.getPresetRoleCode();

                    if (roleCode.equals(RoleEnum.parents.getValue())) {
                        if (Objects.isNull(user.getIsActive()) || !user.getIsActive() || Objects.isNull(user.getConfirmed()) || !user.getConfirmed()) {
                            // 修改家长的激活和确认状态
                            userDetailsService.updateParentConfirmStatus(user.getId());
                        }
                    }
                }
            }
            ResponseUtil.responseSucceed(response, oAuth2AccessToken);
        } catch (BadCredentialsException e) {
            log.error("Login BadCredentialsException:", e);
            //密码错误
            ResponseUtil.responseWriter(response, USER_PASSWORD_ERROR.getMessage(), USER_PASSWORD_ERROR.getCode());
        } catch (InternalAuthenticationServiceException e) {
            log.error("Login InternalAuthenticationServiceException:", e);

            String message = e.getMessage();
            if (message.contains("Load balancer does not have available server")) {
                //服务不可用
                ResponseUtil.responseWriter(response, AVAILABLE_SERVICE.getMessage(), AVAILABLE_SERVICE.getCode());
            } else {
                ResponseUtil.responseWriter(response, message, 600);
            }
        } catch (DisabledException e) {
            log.error("Login DisabledException:", e);
            ResponseUtil.responseWriter(response, USER_DISABLED.getMessage(), USER_DISABLED.getCode());
        } catch (AuthenticationException e) {
            String message = e.getMessage();
            if (message.contains(USER_SELECTION_ERROR.getMessage())) {
                // 角色选择异常
                LoginResult loginResult = JsonUtils.readValue(message.replace(USER_SELECTION_ERROR.getMessage() + ":", StrUtil.EMPTY), LoginResult.class);
                ResponseUtil.responseSucceed(response, new UserSelectionResult(true, loginResult));
            } else {
                ResponseUtil.responseWriter(response, USER_PASSWORD_ERROR.getMessage(), USER_PASSWORD_ERROR.getCode());
            }
        } catch (Exception e) {
            log.error("Login Exception:", e);
            exceptionHandler(response, e);
        }
    }

    @ApiOperation(value = "根据用户id和用户名查找token并移除登录状态")
    @GetMapping("/oauth/removeLogin")
    private void getTokenAndRemoveLogin(Long userId, String username) {
        if (Objects.isNull(userId) || userId == 0L || StringUtils.isBlank(username)) {
            return;
        }

        Collection<OAuth2AccessToken> tokens = tokenStore.findTokensByClientIdAndUserName("app", username);
        for (OAuth2AccessToken accessToken : tokens) {
            OAuth2Authentication oAuth2Authentication = tokenStore.readAuthentication(accessToken);
            Object principal = oAuth2Authentication.getUserAuthentication().getPrincipal();
            if (principal instanceof SysUserDTO) {
                SysUserDTO userInfo = (SysUserDTO)principal;
                if (userInfo.getId().equals(userId)) {
                    OAuth2RefreshToken refreshToken = accessToken.getRefreshToken();
                    if (null != refreshToken) {
                        tokenStore.removeRefreshToken(refreshToken);
                    }
                    tokenStore.removeAccessToken(accessToken);
                }
            }
        }
    }

    @Resource
    private UserFeignClient userFeignClient;

    @ApiOperation(value = "根据用户id和用户名简单测试数据")
    @GetMapping("/oauth/test")
    private CommonResult<Object> getTest(Long choseUserId, String username) {
        return userFeignClient.getLogById(RandomUtil.randomLong(243540));
    }

    @Data
    @AllArgsConstructor
    static class UserSelectionResult {
        private boolean multipleRole;
        private LoginResult loginResult;
    }

    private void exceptionHandler(HttpServletResponse response, Exception e) throws IOException {
        log.error("exceptionHandler-error:", e);
        exceptionHandler(response, e.getMessage());
    }

    private void exceptionHandler(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        ResponseUtil.responseUnAuthorized(response, msg);
    }

    private ClientDetails getClient(String clientId, String clientSecret) {
        ClientDetails clientDetails = clientDetailsService.loadClientByClientId(clientId);

        if (clientDetails == null) {
            throw new UnapprovedClientAuthenticationException("clientId对应的信息不存在");
        }
        //秘钥校验耗时70毫秒左右
        /*else if (!passwordEncoder.matches(clientSecret, clientDetails.getClientSecret())) {
            throw new UnapprovedClientAuthenticationException("clientSecret不匹配");
        }*/
        return clientDetails;
    }
}