package com.cnasoft.health.userservice.service;

import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.CommonDTO;
import com.cnasoft.health.common.dto.MessageDTO;
import com.cnasoft.health.common.dto.RegionStaffInfoDTO;
import com.cnasoft.health.common.dto.SchoolDTO;
import com.cnasoft.health.common.dto.SysUserDTO;
import com.cnasoft.health.common.enums.RoleEnum;
import com.cnasoft.health.common.service.ISuperService;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.feign.dto.AreaManagerCreateReqVO;
import com.cnasoft.health.userservice.feign.dto.AreaManagerUpdateReqVO;
import com.cnasoft.health.userservice.feign.dto.ForgetPasswordVO;
import com.cnasoft.health.userservice.feign.dto.SchoolManagerCreateReqVO;
import com.cnasoft.health.userservice.feign.dto.SchoolManagerUpdateReqVO;
import com.cnasoft.health.userservice.feign.dto.SysUserAuthorityReqVO;
import com.cnasoft.health.userservice.feign.dto.SysUserCreateReqVO;
import com.cnasoft.health.userservice.feign.dto.SysUserReqVO;
import com.cnasoft.health.userservice.feign.dto.SysUserUpdateMobileReqVO;
import com.cnasoft.health.userservice.feign.dto.SysUserUpdateReqVO;
import com.cnasoft.health.userservice.feign.dto.TaskUserReqVO;
import com.cnasoft.health.userservice.feign.dto.TaskUserResVO;
import com.cnasoft.health.userservice.feign.dto.TestManagerReqVO;
import com.cnasoft.health.userservice.feign.dto.UpdatePasswordReqVO;
import com.cnasoft.health.userservice.feign.dto.UpgradeDTO;
import com.cnasoft.health.userservice.model.SchoolManager;
import com.cnasoft.health.userservice.model.SysAuthority;
import com.cnasoft.health.userservice.model.SysUser;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户信息
 *
 * @author ganghe
 */
public interface ISysUserService extends ISuperService<SysUser> {
    /**
     * 根据请求参数（姓名、手机号、区域）获取学校管理员id列表
     *
     * @param query 查询条件
     * @return 学校管理员id列表
     */
    List<Long> getSchoolManagerIdByQuery(String query);

    /**
     * 根据请求参数（姓名、手机号、区域）获取测试管理员id列表
     *
     * @param query 查询条件
     * @return 学校管理员id列表
     */
    List<Long> getTestManagerIdByQuery(String query);

    /**
     * 根据用户id和学校id判断用户是否存在
     *
     * @param userId   用户id
     * @param schoolId 学校id
     * @return 是否存在
     */
    Boolean checkExistsUserByUserIdAndSchoolId(Long userId, Long schoolId);

    /**
     * 根据用户id和区域编码判断用户是否存在
     *
     * @param userId   用户id
     * @param areaCode 区域编码
     * @return 是否存在
     */
    Boolean checkExistsUserByUserIdAndAreaCode(Long userId, Integer areaCode);

    /**
     * 新增校级管理员
     *
     * @param schoolManagerReqVO
     * @return
     * @throws Exception
     */
    SchoolManager saveSchoolManager(SchoolManagerCreateReqVO schoolManagerReqVO) throws Exception;

    /**
     * 新增测试管理员
     *
     * @param testManagerReqVO 请求对象
     * @return
     * @throws Exception
     */
    TestManagerReqVO saveTestManager(TestManagerReqVO testManagerReqVO) throws Exception;

    /**
     * 修改测试管理员
     *
     * @param testManagerReqVO 请求对象
     * @return
     * @throws Exception
     */
    void updateTestManager(TestManagerReqVO testManagerReqVO) throws Exception;

    /**
     * 修改校级管理员
     *
     * @param schoolManagerReqVO
     * @throws Exception
     */
    void updateSchoolManager(SchoolManagerUpdateReqVO schoolManagerReqVO) throws Exception;

    /**
     * 添加区域管理员
     *
     * @param managerCreateReqVO
     * @return
     * @throws Exception
     */
    SysUser saveAreaManager(AreaManagerCreateReqVO managerCreateReqVO) throws Exception;

    /**
     * 修改区域管理员
     *
     * @param managerCreateReqVO
     * @return
     * @throws Exception
     */
    boolean updateAreaManager(AreaManagerUpdateReqVO managerCreateReqVO) throws Exception;

    /**
     * 修改用户信息
     *
     * @param sysUserUpdateReqVO
     * @throws Exception
     */
    void updateUserBaseInfo(SysUserUpdateReqVO sysUserUpdateReqVO) throws Exception;

    /**
     * 添加一级管理员
     *
     * @param createReqVO
     * @return
     * @throws Exception
     */
    boolean saveFirstLevelManager(SysUserCreateReqVO createReqVO) throws Exception;

    /**
     * 添加二级管理员
     *
     * @param createReqVO
     * @return
     * @throws Exception
     */
    boolean saveSecondLevelManager(SysUserCreateReqVO createReqVO) throws Exception;

    /**
     * 根据id查询用户
     *
     * @param id        用户ID
     * @param cacheData 是否缓存数据
     * @return
     */
    SysUserDTO findByUserId(Long id, Boolean cacheData);

    /**
     * 根据id查询用户角色
     *
     * @param id 用户ID
     * @return
     */
    String findRoleCodeById(Long id);

    /**
     * 用户分配角色
     *
     * @param userId
     * @param roleIds
     */
    void setRoleToUser(Long userId, List<String> roleIds);

    /**
     * 用户分配权限
     *
     * @param id
     * @param userAuthorityReqVOS
     */
    void setAuthorityToUser(Long id, Set<SysUserAuthorityReqVO> userAuthorityReqVOS);

    /**
     * 更新用户状态
     *
     * @param id      用户id
     * @param enabled 是否启用
     */
    void updateEnabled(Long id, Boolean enabled);

    /**
     * 更新用户状态
     *
     * @param params
     * @param transactionId
     */
    public void doUpdateEnabled(Map<String, Object> params, String transactionId);

    /**
     * 重置密码
     *
     * @param id
     */
    void resetPassword(Long id);

    /**
     * 忘记密码
     *
     * @param forgetPassword
     */
    void forgetPassword(ForgetPasswordVO forgetPassword);

    /**
     * 根据手机号验证码查找用户并校验验证码
     *
     * @param mobile  手机号
     * @param captcha 验证码
     * @return
     */
    List<SysUserDTO> findLoginUserByMobile(String mobile, String captcha, boolean h5);

    /**
     * 更改短信验证码使用状态
     *
     * @param mobile  手机号
     * @param captcha 验证码
     * @return boolean
     */
    Boolean updatesSmsRecordUsedStatus(String mobile, String captcha);

    /**
     * 更新密码
     *
     * @param updatePasswordReqVO
     */
    void updatePassword(UpdatePasswordReqVO updatePasswordReqVO);

    /**
     * 用户列表
     *
     * @param params
     * @return
     */
    PageResult<SysUserDTO> findUsers(Map<String, Object> params);

    /**
     * 测试管理员用户列表
     *
     * @param params
     * @return
     */
    PageResult<SysUserDTO> findTestUsers(Map<String, Object> params);

    /**
     * 删除用户
     *
     * @param ids
     * @return
     */
    List<BatchOperationTipDTO> delUser(Set<Long> ids);

    /**
     * 删除用户
     *
     * @param users
     * @param transactionId
     * @return
     */
    void doDelUser(Set<SysUser> users, String transactionId);

    /**
     * 用户权限列表
     *
     * @param userId
     * @return
     */
    List<SysAuthority> findAuthoritiesByUserId(Long userId);

    /**
     * 根据id查询用户信息
     *
     * @param id
     * @return
     */
    SysUser selectSysUserById(Long id);

    /**
     * 缓存用户数据
     *
     * @param userId 用户id
     */
    void cacheUser(Long userId);

    /**
     * 发送未读消息
     *
     * @param message message
     */
    void sendMessage(MessageDTO message);

    /**
     * 查询任务用户
     *
     * @param query 查询实体
     * @return
     */
    PageResult<TaskUserResVO> getTaskUsers(TaskUserReqVO query) throws Exception;

    /**
     * 批量插入账号
     *
     * @param sysUsers
     * @return
     */
    int saveBatch(List<SysUser> sysUsers);

    /**
     * 根据用户id列表查询用户数据
     *
     * @param ids
     * @return
     */
    List<SysUser> findListByIds(List<Long> ids);

    /**
     * 根据角色编码查询拥有该角色的用户登录账号
     *
     * @param roleCode 角色编码
     * @return
     */
    List<Long> getUserIdListByRoleCode(String roleCode);

    /**
     * 获取区域职员的用户信息
     *
     * @param userId 用户Id
     * @return RegionStaffInfoDTO
     */
    RegionStaffInfoDTO getRegionStaffInfo(Long userId);

    /**
     * 删除区域管理员
     *
     * @param ids 用户id
     */
    void deleteAreaManager(Set<Long> ids);

    /**
     * 删除校级管理员
     *
     * @param ids 用户id
     */
    void deleteSchoolManager(Set<Long> ids);

    /**
     * 删除测试管理员
     *
     * @param ids 用户id
     */
    void deleteTestManager(Set<Long> ids);

    /**
     * 根据学校id删除校级管理员
     *
     * @param schoolId 学校id
     */
    void deleteSchoolManagerBySchool(Long schoolId);

    /**
     * 根据query条件（姓名手机区域）查询区域管理员
     *
     * @param query    query 查询条件
     * @param areaType 区域类型
     * @return 区域管理员id列表
     */
    List<Long> getAreaManagerIdByQuery(String query, Integer areaType);

    /**
     * 区心理教研员/校心理老师是否有查看该用户测评结果的数据权限
     *
     * @param taskUserId 测评记录的用户id
     * @return
     */
    Boolean taskInScope(Long taskUserId);

    Long findUserCount();

    List<String> findUsernameAndMobile(Integer limit, Integer offset);

    /**
     * 根据用户名或者手机号或者短id查询用户
     *
     * @param query 查询条件
     * @return 用户列表
     */
    List<SysUserDTO> findUserByUsernameOrMobileOrShortId(String query);

    /**
     * 根据手机号查询测试管理员用户信息
     *
     * @param query 查询条件
     * @return 用户数据
     */
    SysUserDTO findUserByUsernameWithTestManager(String query);

    /**
     * 保存用户信息
     *
     * @param user     用户对象
     * @param roleEnum 角色枚举
     * @return
     * @throws Exception 异常
     */
    boolean saveUserPublic(SysUser user, RoleEnum roleEnum) throws Exception;

    /**
     * 保存用户信息
     *
     * @param sysUser
     * @param transactionId
     */
    void doSaveUserPublic(SysUser sysUser, String transactionId);

    /**
     * 更新用户信息
     *
     * @param user 用户对象
     * @return
     * @throws Exception 异常
     */
    boolean updateUserPublic(SysUser user) throws Exception;

    /**
     * h5端修改用户手机号
     *
     * @param vo
     */
    void h5UpdateUserMobile(SysUserUpdateMobileReqVO vo) throws Exception;

    /**
     * h5端修改用户姓名，性别
     *
     * @param vo
     * @throws Exception
     */
    void h5UpdateUserInfo(SysUserReqVO vo) throws Exception;

    /**
     * 修改用户信息(邮箱)
     *
     * @param vo
     */
    void updateUserInfo(SysUserReqVO vo);

    CommonResult<List<String>> h5FileUpload(MultipartFile[] files);

    /**
     * 根据学校id批量删除用户基本信息
     *
     * @param schoolId 学校id
     */
    void deleteBySchoolId(Long schoolId);

    /**
     * 根据区域编码批量删除用户基本信息
     *
     * @param areaCode 区域编码
     */
    void deleteByAreaCode(Integer areaCode);

    /**
     * 获取测试管理员管理的学校数据
     *
     * @return
     */
    List<SchoolDTO> findSchoolByTestManager();

    /**
     * 根据用户id获取用户头像
     *
     * @param userId
     * @return
     */
    String getHeadImgUrlById(Long userId);

    /**
     * 更新当前用户头像
     *
     * @param headImgUrl 头像地址
     * @return
     */
    String updateHeadImg(String headImgUrl);

    /**
     * 根据PAD当前版本号判断是否需要升级，需要升级则返回APP下载地址
     *
     * @param currentVersion PAD当前版本号
     * @return 版本升级信息
     */
    UpgradeDTO getPadUpgradeUrl(Integer currentVersion);

    /**
     * 获取第三方资源平台授权code
     *
     * @return
     */
    String getResourcePlatformCode() throws Exception;

    /**
     * 用户数据增量统计(当年)
     *
     * @return 统计数据
     */
    Map<String, List<Map<String, Object>>> getUserStatistics();

    /**
     * 获取某个学校的学生/家长/教职工的数量
     *
     * @return 用户数量
     */
    Integer getUserCountBySchoolId(Long schoolId, Integer userRoleType);

    /**
     * 获取某个区域的学生/家长/教职工/区域职工的数量
     *
     * @return 用户数量
     */
    Integer getUserCountByAreaCode(String areaCode, Integer userRoleType);

    /**
     * 获取学校各类人员数量
     *
     * @return 各类用户数量
     */
    Map<String, List<Map<String, Object>>> getTotalUserNumOfSchool(Long schoolId);

    /**
     * 获取区域下 学校各类人员数量  区域人员数量
     *
     * @return 各类用户数量
     */
    Map<String, List<Map<String, Object>>> getTotalUserNumOfArea(String areaCode, Long wholeAreaCode);

    /**
     * 查询用户姓名
     *
     * @param userId 用户id
     * @return 用户姓名
     */
    String findNameById(Long userId);

    /**
     * 根据id列表查询用户名称(name)
     *
     * @param userIds 用户id列表
     * @return 用户信息
     */
    List<CommonDTO> findNameByIds(Set<Long> userIds);

    /**
     * 根据查询条件获取区域用户id列表
     *
     * @param params 查询条件
     * @return 区域用户id列表
     */
    List<Long> getAreaUserIdByParams(Map<String, Object> params);

    /**
     * 根据姓名模糊查询所有用户(未删除且已审核的)的userId列表,只查询超管和二级管理员
     *
     * @param name 姓名
     * @return 用户id列表
     */
    List<Long> findUserIdListByName(String name, List<String> roleCodes);

    /**
     * 根据姓名、角色、区域编码、学校ID 查询学生用户id列表
     *
     * @param params
     * @return
     */
    List<Long> getUserIdsByQuery(Map<String, Object> params);
}
