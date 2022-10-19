package com.cnasoft.health.userservice.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.dto.ParentDTO;
import com.cnasoft.health.common.dto.ParentStudentDTO;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.feign.dto.ParentBaseRespVO;
import com.cnasoft.health.userservice.feign.dto.ParentRespVO;
import com.cnasoft.health.userservice.feign.dto.TaskUserReqVO;
import com.cnasoft.health.userservice.feign.dto.TaskUserResVO;
import com.cnasoft.health.userservice.model.Parent;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zcb
 * @description 针对表【parent(家长表)】的数据库操作Mapper
 * @date 2022-03-24
 */
public interface ParentMapper extends SuperMapper<Parent> {
    /**
     * 根据条件查询查询单个家长信息
     *
     * @param params 查询条件
     * @param key    秘钥
     * @return 家长数据
     */
    Parent selectOneByParams(@Param("params") Map<String, Object> params, @Param("key") String key);

    /**
     * 后台管理，家长列表
     *
     * @param page   分页参数
     * @param params 查询条件
     * @param key    秘钥
     * @return 家长列表
     */
    List<ParentRespVO> pageList(Page<ParentRespVO> page, @Param("params") Map<String, Object> params, @Param("key") String key);

    /**
     * 后台管理，查询家长总数
     *
     * @param params 查询条件
     * @param key    秘钥
     * @return 家长总数
     */
    Long pageListCount(@Param("params") Map<String, Object> params, @Param("key") String key);

    /**
     * 发起任务时，查询家长列表
     *
     * @param page  分页对象
     * @param query 查询条件
     * @param key   秘钥
     * @return 家长列表
     */
    List<TaskUserResVO> getTaskUsers(Page<TaskUserResVO> page, @Param("query") TaskUserReqVO query, @Param("key") String key);

    /**
     * 批量保存家长数据
     *
     * @param parents 家长数据
     * @param key     秘钥
     * @return 受影响的行数
     */
    @Override
    int insertBatch(@Param("list") List<Parent> parents, @Param("key") String key);

    /**
     * 批量更新家长数据
     *
     * @param parents 家长数据
     * @param key     key
     * @return 受影响的行数
     */
    @Override
    int updateBatch(@Param("list") List<Parent> parents, @Param("key") String key);

    /**
     * 根据用户名和学校id查询家长
     *
     * @param schoolId  学校id
     * @param usernames 用户名列表
     * @param key       加密key
     * @return 家长列表
     */
    List<Parent> selectByParent(@Param("schoolId") Long schoolId, @Param("usernames") List<String> usernames, @Param("key") String key);

    /**
     * 查询某个家长的详细信息
     *
     * @param mobile   家长电话
     * @param schoolId 学校id
     * @param key      加密key
     * @return 家长数据
     */
    Parent findByMobile(@Param("mobile") String mobile, @Param("schoolId") Long schoolId, @Param("key") String key);

    /**
     * 查询家长及其学生信息(简单信息)
     *
     * @param userId   家长用户id
     * @param schoolId 学校id
     * @param key      秘钥
     * @return 家长数据
     */
    ParentRespVO findInfo(@Param("userId") Long userId, @Param("schoolId") Long schoolId, @Param("key") String key);

    /**
     * 查询家长的子女信息
     *
     * @param id  家长id
     * @param key 秘钥
     * @return 学生用户id列表
     */
    List<Long> getChildrenUserId(@Param("id") Long id, @Param("key") String key);

    /**
     * 查询家长的子女所在学校id列表
     *
     * @param userId 家长用户id
     * @return 学校id列表
     */
    Set<Long> getChildrenSchoolId(@Param("userId") Long userId);

    /**
     * 查询家长的激活状态和确认状态
     *
     * @param userIds 用户id列表
     * @return map对象
     */
    @MapKey("id")
    Map<Long, Map<String, Boolean>> getConfirmAndActive(@Param("userIds") List<Long> userIds);

    /**
     * 查询家长及其学生信息(简单信息)
     *
     * @param userId   用户id
     * @param schoolId 学校id
     * @param key      秘钥
     * @return 家长数据
     */
    ParentRespVO findInfoByUserId(@Param("userId") Long userId, @Param("schoolId") Long schoolId, @Param("key") String key);

    /**
     * 获取家长下拉列表
     *
     * @param schoolId 学校id
     * @param userName 姓名
     * @param key      秘钥
     * @return 心理教研员列表信息
     */
    List<Map<String, Object>> getSelectList(@Param("schoolId") Long schoolId, @Param("userName") String userName, @Param("key") String key);

    /**
     * 分页获取学生基础信息
     *
     * @param page  分页参数
     * @param param 查询条件
     * @param key   秘钥
     * @return 家长信息
     */
    List<ParentBaseRespVO> listBaseInfo(Page<ParentRespVO> page, @Param("param") Map<String, Object> param, @Param("key") String key);

    /**
     * 根据条件查询查询家长用户id
     *
     * @param params 查询条件
     * @param key    秘钥
     * @return 家长用户id列表
     */
    List<Long> getParentUserIdByParams(@Param("params") Map<String, Object> params, @Param("key") String key);

    /**
     * 根据用户id查询家长基本信息及子女信息
     *
     * @param userId 用户id
     * @param key    秘钥
     * @return 家长信息
     */
    ParentStudentDTO findParentInfo(@Param("userId") Long userId, @Param("key") String key);

    /**
     * 根据用户id列表查询家长基本信息及子女姓名
     *
     * @param userIds 用户id列表
     * @param key     秘钥
     * @return 家长信息
     */
    List<ParentDTO> findParentList(@Param("userIds") Set<Long> userIds, @Param("key") String key);
}