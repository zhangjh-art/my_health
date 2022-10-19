package com.cnasoft.health.userservice.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.dto.CommonDTO;
import com.cnasoft.health.common.dto.RegionStaffInfoDTO;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.model.School;
import com.cnasoft.health.userservice.model.SysUser;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author ganghe
 */
@DS(Constant.DATA_SOURCE_MYSQL)
@Component
public interface SysUserMapper extends SuperMapper<SysUser> {

    /**
     * 根据用户id查询用户信息
     *
     * @param key 秘钥
     * @param id  id
     * @return 用户
     */
    SysUser selectOneById(@Param("key") String key, @Param("id") Long id);

    /**
     * 根据条件查询存在的数据总数量
     *
     * @param params 查询条件
     * @param key    秘钥
     * @return 行数
     */
    int getCount(@Param("param") Map<String, Object> params, @Param("key") String key);

    /**
     * 分页查询用户列表
     *
     * @param page   分页对象
     * @param params 查询条件
     * @param key    key
     * @return 用户账号列表
     */
    List<SysUser> findList(Page<SysUser> page, @Param("u") Map<String, Object> params, @Param("key") String key);

    /**
     * 分页查询区域管理员用户列表
     *
     * @param page   分页对象
     * @param params 查询条件
     * @param key    key
     * @return 用户账号列表
     */
    List<SysUser> findAreaManagerList(Page<SysUser> page, @Param("u") Map<String, Object> params, @Param("key") String key);

    /**
     * 分页查询校级管理员用户列表
     *
     * @param page   分页对象
     * @param params 查询条件
     * @param key    key
     * @return 用户账号列表
     */
    List<SysUser> findSchoolManagerList(Page<SysUser> page, @Param("u") Map<String, Object> params, @Param("key") String key);

    /**
     * 分页查询测试管理员用户列表
     *
     * @param page   分页对象
     * @param params 查询条件
     * @param key    key
     * @return 用户账号列表
     */
    List<SysUser> findTestManagerList(Page<SysUser> page, @Param("u") Map<String, Object> params, @Param("key") String key);

    /**
     * 获取校级管理员对应的学校
     *
     * @param userId
     * @return
     */
    School findAreaCode(Long userId);

    /**
     * 查询某个用户
     *
     * @param sysUser 查询条件
     * @param key     key
     * @return SysUser
     */
    SysUser findOne(@Param("u") SysUser sysUser, @Param("key") String key);

    /**
     * 根据用户id列表查询用户信息
     *
     * @param ids 用户id列表
     * @param key key
     * @return 用户列表
     */
    List<SysUser> findListByIds(@Param("ids") List<Long> ids, @Param("key") String key);

    /**
     * 根据角色查询拥有该角色的用户登录账号
     *
     * @param roleCode 角色编码
     * @param key      key
     * @return 登录账号列表
     */
    List<Long> getUserIdListByRoleId(@Param("roleCode") String roleCode, @Param("key") String key);

    /**
     * 批量插入账号
     *
     * @param sysUsers 用户集合
     * @param key      秘钥
     * @return 行数
     */
    @Override
    int insertBatch(@Param("list") List<SysUser> sysUsers, @Param("key") String key);

    /**
     * 根据用户名查询账号信息
     *
     * @param usernames 用户名列表
     * @param key       key
     * @return 用户列表
     */
    List<SysUser> selectByUsernames(@Param("usernames") List<String> usernames, @Param("key") String key);

    /**
     * 查询用户角色编码
     *
     * @param id 用户id
     * @return
     */
    String selectRoleCode(@Param("id") Long id);

    /**
     * 批量更新账号
     *
     * @param sysUsers 用户集合
     * @param key      秘钥
     * @return 行数
     */
    @Override
    int updateBatch(@Param("list") List<SysUser> sysUsers, @Param("key") String key);

    /**
     * 获取区域职员的部门岗位信息
     *
     * @param userId 用户id
     * @return RegionStaffInfoDTO
     */
    RegionStaffInfoDTO getRegionStaffInfo(@Param("userId") Long userId);

    /**
     * 通过身份证判断学生是否已存在
     *
     * @param identityCardNumber 身份证
     * @param neUserId           userId != neUserId
     * @return 行数
     */
    Integer studentExisted(@Param("identityCardNumber") String identityCardNumber, @Param("neUserId") Long neUserId);

    /**
     * 获取用户邮箱
     *
     * @param userId 用户id
     * @return 邮箱
     */
    String getUserEmail(@Param("id") Long userId);

    /**
     * 根据请求参数（姓名、手机号、区域）获取学校管理员id列表
     *
     * @param query query
     * @param key   秘钥
     * @return 学校管理员id列表
     */
    List<Long> getSchoolManagerIdByQuery(@Param("query") String query, @Param("key") String key);

    /**
     * 根据请求参数（姓名、手机号、区域）获取测试管理员id列表
     *
     * @param query query
     * @param key   秘钥
     * @return 学校管理员id列表
     */
    List<Long> getTestManagerIdByQuery(@Param("query") String query, @Param("key") String key);

    /**
     * 根据请求参数（姓名、手机号、区域）获取区域管理员id列表
     *
     * @param query    query
     * @param key      秘钥
     * @param areaType 区域类型
     * @return 区域管理员id列表
     */
    List<Long> getAreaManagerIdByQuery(@Param("query") String query, @Param("key") String key, @Param("areaType") Integer areaType);

    /**
     * 分页读取用户表用户名和手机号
     *
     * @param limit  limit
     * @param offset offset
     * @param key    key
     * @return List<String>
     */
    List<String> findUsernameAndMobile(@Param("limit") Integer limit, @Param("offset") Integer offset, @Param("key") String key);

    /**
     * 根据用户名或者手机号或者短id查询用户
     * 默认筛选 mobile，flag为 true 同时筛选 username和 short_id
     *
     * @param query query
     * @param key   key
     * @param flag  是否包含用户名和短id
     * @return List<SysUserDTO>
     */
    List<SysUser> findUserByUsernameOrMobileOrShortId(@Param("query") String query, @Param("key") String key, @Param("flag") String flag);

    /**
     * 根据手机号查询测试管理员用户信息
     *
     * @param query 查询条件
     * @param key   key
     * @return
     */
    SysUser findUserByUsernameWithTestManager(@Param("query") String query, @Param("key") String key);

    /**
     * 判断相同username、role在不同区域/学校的可用账户是否存在
     *
     * @param username
     * @param role
     * @return
     */
    SysUser getUserByUsernameRoleAreaSchool(@Param("username") String username, @Param("role") String role, @Param("area") Integer areaCode, @Param("schoolId") Long schoolId,
        @Param("key") String key);

    /**
     * 根据家长用户id获取对应学生的userId列表
     *
     * @param userId
     * @return
     */
    List<Long> selectChildrenUserIdsByParentUserId(@Param("userId") Long userId);

    /**
     * 更新学生短id
     *
     * @param sysUser 用户对象
     * @param key     key
     * @return 受影响的行数
     */
    int updateShortId(@Param("u") SysUser sysUser, @Param("key") String key);

    /**
     * 获取用户id
     *
     * @param schoolId 学校id
     * @return
     */
    Set<Long> getUserIdBySchool(@Param("schoolId") Long schoolId);

    /**
     * 根据用户id和学校id判断用户是否存在
     *
     * @param userId   用户id
     * @param schoolId 学校id
     * @return 是否存在
     */
    Boolean checkExistsUserByUserIdAndSchoolId(@Param("userId") Long userId, @Param("schoolId") Long schoolId);

    /**
     * 根据用户id和区域编码判断用户是否存在
     *
     * @param userId   用户id
     * @param areaCode 区域编码
     * @return 是否存在
     */
    Boolean checkExistsUserByUserIdAndAreaCode(@Param("userId") Long userId, @Param("areaCode") Integer areaCode);

    /**
     * 获取用户id
     *
     * @param areaCode 区域编码
     * @return
     */
    Set<Long> getUserIdByAreaCode(@Param("areaCode") Integer areaCode);

    /**
     * 根据PAD当前版本号判断是否需要升级，需要升级则返回APP下载地址
     *
     * @param currentVersion PAD当前版本号
     * @return 下载地址
     */
    String getPadUpgradeUrl(@Param("currentVersion") Integer currentVersion);

    /**
     * 区域管理员增量统计(当年)
     *
     * @param date 统计开始时间
     * @return
     */
    List<Map<String, Object>> getAreaManagerStatistics(@Param("date") String date);

    /**
     * 学校管理员增量统计(当年)
     *
     * @param date 统计开始时间
     * @return
     */
    List<Map<String, Object>> getSchoolManagerStatistics(@Param("date") String date);

    /**
     * 根据区域编码查询区域用户数量
     *
     * @param areaCode     模糊编码 省两位 市四位
     * @param userRoleType 用户角色
     * @return
     */
    int getAreaUserCount(@Param("areaCode") String areaCode, @Param("userRoleType") Integer userRoleType);

    /**
     * 获取学校下各年级学生数量
     *
     * @param schoolId 学校Id
     * @return
     */
    List<Map<String, Object>> getGradesStudentNum(@Param("schoolId") Long schoolId);

    /**
     * 根据学校下各年级家长数量
     *
     * @param schoolId 学校Id
     * @return
     */
    List<Map<String, Object>> getGradesParentNum(@Param("schoolId") Long schoolId);

    /**
     * 根据学校下各部门员工数量
     *
     * @param schoolId 学校Id
     * @return
     */
    List<Map<String, Object>> getDepartmentStaffNum(@Param("schoolId") Long schoolId);

    /**
     * 根据学校下心理咨询师数量
     *
     * @param schoolId 学校Id
     * @return
     */
    List<Map<String, Object>> getSchoolTeacherNum(@Param("schoolId") Long schoolId);

    /**
     * 根据区域下各学校学生数量
     *
     * @param areaCode 简略区域ID
     * @return
     */
    List<Map<String, Object>> getAreaStudentNum(@Param("areaCode") String areaCode);

    /**
     * 根据区域下各学校家长数量
     *
     * @param areaCode 简略区域ID
     * @return
     */
    List<Map<String, Object>> getAreaParentNum(@Param("areaCode") String areaCode);

    /**
     * 根据区域下各学校员工数量
     *
     * @param areaCode 简略区域ID
     * @return
     */
    List<Map<String, Object>> getAreaSchoolStaffNum(@Param("areaCode") String areaCode);

    /**
     * 根据区域各部门员工数量
     *
     * @param wholeAreaCode 区域Id
     * @return
     */
    List<Map<String, Object>> getAreaStaffNum(@Param("wholeAreaCode") Long wholeAreaCode);

    /**
     * 根据区域心理咨询师数量
     *
     * @param wholeAreaCode 区域Id
     * @return
     */
    List<Map<String, Object>> getAreaTeacherNum(@Param("wholeAreaCode") Long wholeAreaCode);

    /**
     * 查询用户姓名
     *
     * @param userId 用户id
     * @param key    秘钥
     * @return 用户姓名
     */
    String findNameById(@Param("userId") Long userId, @Param("key") String key);

    /**
     * 根据id列表查询用户名称(name)
     *
     * @param userIds 用户id列表
     * @param key     秘钥
     * @return 用户信息
     */
    List<CommonDTO> findNameByIds(@Param("userIds") Set<Long> userIds, @Param("key") String key);

    /**
     * 根据查询条件获取区域用户id列表
     *
     * @param params 查询条件
     * @param key    秘钥
     * @return 用户id和班级id
     */
    List<Long> getAreaUserIdByParams(@Param("params") Map<String, Object> params, @Param("key") String key);

    /**
     * 根据姓名模糊查询所有用户(未删除且已审核的)的userId列表
     *
     * @param name      姓名
     * @param roleCodes 用户角色code列表
     * @param key       秘钥
     * @return 用户id列表
     */
    List<Long> findUserIdListByName(@Param("name") String name, @Param("roleCodes") List<String> roleCodes, @Param("key") String key);

    /**
     * 修改数据状态为正常业务可用状态
     *
     * @param id
     */
    void fullData(@Param("id") Long id);

    /**
     * 根据姓名、角色、区域编码、学校ID 查询学生用户id列表
     * @param params
     * @param key
     * @return
     */
    List<Long> getUserIdsByQuery(@Param("params") Map<String, Object> params, @Param("key") String key);
}
