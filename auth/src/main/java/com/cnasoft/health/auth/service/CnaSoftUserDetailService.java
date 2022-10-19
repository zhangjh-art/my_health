package com.cnasoft.health.auth.service;

import com.cnasoft.health.common.dto.SysUserDTO;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collection;
import java.util.List;

/**
 * @author cnasoft
 * @date 2020/6/30 13:27
 */
public interface CnaSoftUserDetailService extends UserDetailsService {

    /**
     * 根据手机号验证码查找用户并校验验证码
     *
     * @param mobile  手机号
     * @param captcha 验证码
     * @return 用户列表
     */
    List<SysUserDTO> loadUserByMobile(String mobile, String captcha);

    /**
     * 更改短信验证码使用状态
     *
     * @param mobile  手机号
     * @param captcha 验证码
     * @return boolean
     */
    Boolean updatesSmsRecordUsedStatus(String mobile, String captcha);

    /**
     * 根据openId查找用户
     *
     * @param openId openId
     * @return 用户对象
     */
    UserDetails loadUserByOpenId(String openId);

    /**
     * 更新家长激活状态和确认状态
     *
     * @param userId 用户id
     */
    void updateParentConfirmStatus(Long userId);

    /**
     * 将用户的用户名、手机号作为key添加到filter中
     *
     * @param keys 用户名和手机号列表
     */
    void addUserToFilter(Collection<String> keys);

    /**
     * 查询用户名、手机号、短id符合的用户
     *
     * @param username 用户名
     * @return 用户列表
     */
    List<SysUserDTO> findAllUserInUsernameOrMobileOrShortId(String username);

    /**
     * 根据手机号查询测试管理员用户信息
     *
     * @param username
     * @return
     */
    SysUserDTO findUserByUsernameWithTestManager(String username);

    /**
     * 根据用户id查询用户信息
     *
     * @param id 用户Id
     * @return 用户数据
     */
    SysUserDTO findUserById(Long id);

    /**
     * 检查用户合法性
     *
     * @param sysUser 用户信息
     * @return 是否合法
     */
    boolean checkUser(final SysUserDTO sysUser);

    /**
     * 校验密码
     *
     * @param user
     * @param password
     * @return
     */
    boolean pwdCheck(SysUserDTO user, String password);
}
