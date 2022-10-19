package com.cnasoft.health.userservice.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.dto.StudentBaseInfoDTO;
import com.cnasoft.health.common.dto.StudentDTO;
import com.cnasoft.health.common.vo.UserClazzVO;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.feign.dto.BindStudentVO;
import com.cnasoft.health.userservice.feign.dto.StudentBaseRespVO;
import com.cnasoft.health.userservice.feign.dto.StudentInfoRespVO;
import com.cnasoft.health.userservice.feign.dto.StudentRespVO;
import com.cnasoft.health.userservice.feign.dto.TaskUserReqVO;
import com.cnasoft.health.userservice.feign.dto.TaskUserResVO;
import com.cnasoft.health.userservice.model.StudentBaseInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Administrator
 */
@DS(Constant.DATA_SOURCE_MYSQL)
public interface StudentBaseInfoMapper extends SuperMapper<StudentBaseInfo> {

    /**
     * 发起任务查询学生列表
     *
     * @param page  分页条件
     * @param query 查询条件
     * @return
     */
    List<TaskUserResVO> getTaskUsers(Page<TaskUserResVO> page, @Param("query") TaskUserReqVO query, @Param("key") String key);

    /**
     * 根据身份号码查询，学生是否存在
     *
     * @param idCards  身份证id列表
     * @param schoolId 学校id
     * @param key      key
     * @return
     */
    List<StudentBaseInfo> findIdCardNums(@Param("list") List<String> idCards, @Param("schoolId") Long schoolId, @Param("key") String key);

    /**
     * 查询单条学生数据
     *
     * @param params 查询条件
     * @param key    秘钥
     * @return
     */
    StudentBaseInfo selectOneByParams(@Param("param") Map<String, Object> params, @Param("key") String key);

    /**
     * 根据学生身份证号更新家长信息
     *
     * @param studentBaseInfos
     * @return
     */
    int updateParentIdByIdCardNum(@Param("list") List<StudentBaseInfo> studentBaseInfos);

    /**
     * 查询学生列表
     *
     * @param page
     * @param params
     * @return
     */
    List<StudentRespVO> list(Page<StudentBaseInfo> page, @Param("param") Map<String, Object> params, @Param("key") String key);

    /**
     * 查询学生总数
     *
     * @param params
     * @return
     */
    Long listCount(@Param("param") Map<String, Object> params, @Param("key") String key);

    /**
     * 家长列表查询其学生信息
     *
     * @param studentQuery
     * @return
     */
    List<TaskUserResVO> getStudents(TaskUserReqVO studentQuery, @Param("key") String key);

    StudentInfoRespVO getById(@Param("id") Long userId, @Param("schoolId") Long schoolId, @Param("key") String key);

    /**
     * 根据姓名和身份证号查询学生
     *
     * @param students 学生列表
     * @param key      key
     * @return
     */
    List<StudentBaseInfo> getStudent(@Param("students") List<BindStudentVO> students, @Param("key") String key);

    /**
     * 根据用户名和学校id查询学生
     *
     * @param schoolId  学校id
     * @param usernames 用户名列表
     * @param key       加密key
     * @return 学生列表
     */
    List<StudentBaseInfo> selectByStudent(@Param("schoolId") Long schoolId, @Param("usernames") List<String> usernames, @Param("key") String key);

    /**
     * 批量插入学生基础信息
     *
     * @param studentBaseInfos 学生基础信息列表
     * @param key              秘钥
     * @return 行数
     */
    @Override
    int insertBatch(@Param("list") List<StudentBaseInfo> studentBaseInfos, @Param("key") String key);

    /**
     * 批量更新学生基础信息
     *
     * @param studentBaseInfos 学生基础信息列表
     * @param key              秘钥
     * @return 行数
     */
    @Override
    int updateBatch(@Param("list") List<StudentBaseInfo> studentBaseInfos, @Param("key") String key);

    /**
     * 获取学生基本信息
     *
     * @param userId   用户id
     * @param schoolId 学校id
     * @param key      加密key
     * @return 学生基本信息
     */
    StudentBaseInfo findByUserId(@Param("userId") Long userId, @Param("schoolId") Long schoolId, @Param("key") String key);

    /**
     * 更新学生信息 仅更新名称、家长关系
     *
     * @param baseInfo
     * @param key
     */
    void updateStudentBaseInfo(@Param("baseInfo") StudentBaseInfo baseInfo, @Param("key") String key);

    /**
     * 更新学生信息 仅更新邮件等
     */
    void updateCurrentStudentBaseInfo(@Param("baseInfo") StudentBaseInfo baseInfo);

    /**
     * 获取学生id
     *
     * @param schoolId 学校id
     * @return
     */
    Set<Long> getStudentIdBySchool(@Param("schoolId") Long schoolId);

    /**
     * 获取学生id
     *
     * @param clazzId 班级id
     * @return
     */
    Set<Long> getStudentIdByClazz(@Param("clazzId") Long clazzId);

    /**
     * 获取用户id
     *
     * @param clazzId 班级id
     * @return
     */
    Set<Long> getUserIdByClazz(@Param("clazzId") Long clazzId);

    /**
     * 根据用户id列表查询学生信息
     *
     * @param userIds 用户id列表
     * @param key     秘钥
     * @return
     */
    List<StudentBaseInfoDTO> getStudentListByUserIds(@Param("userIds") Set<Long> userIds, @Param("key") String key);

    /**
     * 根据姓名、学号、年级、班级、测试管理员用户id查询学生用户id列表
     *
     * @param params 查询条件
     * @param key    秘钥
     * @return
     */
    List<Long> getUserIdsByQuery(@Param("params") Map<String, Object> params, @Param("key") String key);

    /**
     * 根据姓名查询学生用户id列表
     *
     * @param name 姓名
     * @param key  秘钥
     * @return
     */
    List<Long> getUserIdsByName(@Param("name") String name, @Param("key") String key);

    /**
     * 获取学生下拉列表
     *
     * @param schoolId      学校id
     * @param userName      姓名
     * @param clazzId       班级id
     * @param idCard        身份证号
     * @param studentNumber 学号
     * @return 学生下拉列表
     */
    List<Map<String, Object>> getSelectList(@Param("schoolId") Long schoolId, @Param("userName") String userName, @Param("clazzId") Long clazzId, @Param("idCard") String idCard,
        @Param("studentNumber") String studentNumber, @Param("key") String key);

    /**
     * 获取学生基础信息
     *
     * @param page
     * @param param
     * @param key
     * @return
     */
    List<StudentBaseRespVO> listBaseInfo(Page<StudentBaseInfo> page, @Param("param") Map<String, Object> param, @Param("key") String key);

    /**
     * 根据用户id查询学生信息
     *
     * @param userId 用户id
     * @param key    秘钥
     * @return
     */
    StudentBaseInfoDTO getStudentListByUserId(@Param("userId") Long userId, @Param("key") String key);

    /**
     * 根据条件查询学生用户id
     *
     * @param params 查询条件
     * @param key    秘钥
     * @return 学生用户id
     */
    List<Long> getStudentUserIdByParams(@Param("params") Map<String, Object> params, @Param("key") String key);

    /**
     * 根据学校和班级查询学生用户id和班级id
     *
     * @param schoolId 学校id
     * @param clazzIds 班级id列表
     * @return
     */
    List<UserClazzVO> getStudentUserIdClazzIdBySchoolAndClass(@Param("schoolId") Long schoolId, @Param("clazzIds") List<Long> clazzIds);

    /**
     * 根据用户id查询学生基本信息
     *
     * @param userIds 用户id
     * @param key     秘钥
     * @return 学生信息
     */
    List<StudentDTO> findStudentListByIds(@Param("userIds") Set<Long> userIds, @Param("key") String key);

    /**
     * 获取学生的年级和身份证号
     *
     * @param userId 用户id
     * @param key    秘钥
     * @return 年级和身份证号
     */
    StudentDTO findStudentGradeAndIDNumber(@Param("userId") Long userId, @Param("key") String key);

    /**
     * 根据用户id查询学生基本信息
     *
     * @param userId 用户id
     * @param key    秘钥
     * @return 学生信息
     */
    StudentDTO findStudentInfo(@Param("userId") Long userId, @Param("key") String key);

    /**
     * 根据家长用户id查询学生用户id列表
     *
     * @param userId 家长用户id
     * @return 学生用户id列表
     */
    List<Long> findStudentUserIdByParentUserId(@Param("userId") Long userId);
}
