package com.cnasoft.health.userservice.feign;

import com.cnasoft.health.common.constant.SecurityConstants;
import com.cnasoft.health.common.constant.ServiceNameConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

/**
 * @author: lgf
 * @created: 2022/5/26
 */
@FeignClient(name = ServiceNameConstants.AUTH_SERVICE, decode404 = true)
public interface AuthFeign {

    /**
     * 将新添加的用户名及手机号放入过滤器中
     *
     * @param usernameAndMobileList
     */
    @PostMapping(SecurityConstants.NEW_USER_FILTER_CACHE)
    void addUserToFilter(@RequestBody Set<String> usernameAndMobileList);

    /**
     * 根据用户id和用户名查找token并移除登录状态
     *
     * @param userId   用户id
     * @param username 登录名
     * @return
     */
    @GetMapping(value = "/oauth/removeLogin")
    @Deprecated
    void getTokenAndRemoveLogin(@RequestParam("userId") Long userId, @RequestParam("username") String username);
}
