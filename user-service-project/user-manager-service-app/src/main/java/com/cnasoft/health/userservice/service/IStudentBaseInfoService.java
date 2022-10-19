package com.cnasoft.health.userservice.service;

import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.StudentBaseInfoDTO;
import com.cnasoft.health.common.dto.StudentDTO;
import com.cnasoft.health.common.service.ISuperService;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.common.vo.UserClazzVO;
import com.cnasoft.health.userservice.feign.dto.StudentBaseInfoRespVO;
import com.cnasoft.health.userservice.feign.dto.StudentBaseReqVO;
import com.cnasoft.health.userservice.feign.dto.StudentBaseRespVO;
import com.cnasoft.health.userservice.feign.dto.StudentInfoRespVO;
import com.cnasoft.health.userservice.feign.dto.StudentRespVO;
import com.cnasoft.health.userservice.feign.dto.StudentSaveVO;
import com.cnasoft.health.userservice.model.StudentBaseInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zcb
 */
public interface IStudentBaseInfoService extends ISuperService<StudentBaseInfo> {

    /**
     * 添加学生
     *
     * @param student 学生数据
     * @return 学生基本信息
     * @throws Exception 异常信息
     */
    StudentBaseInfo create(StudentSaveVO student) throws Exception;

    /**
     * 修改学生
     *
     * @param student 学生数据
     * @throws Exception 异常信息
     */
    void updateStudent(StudentSaveVO student) throws Exception;

    /**
     * 批量删除学生
     *
     * @param ids id列表
     * @return 公共信息
     */
    List<BatchOperationTipDTO> delete(Set<Long> ids);

    /**
     * 查询学生列表
     *
     * @param params 查询条件
     * @return 学生分页数据
     */
    PageResult<StudentRespVO> list(Map<String, Object> params);

    /**
     * 根据id查询学生信息
     *
     * @param userId 学生用户id
     * @return 学生信息
     */
    StudentInfoRespVO info(Long userId);

    /**
     * 根据id查询学生信息
     *
     * @param userId 学生用户id
     * @return 学生信息
     */
    StudentInfoRespVO infoOnlyById(Long userId);

    /**
     * 根据id查询学生基础信息
     *
     * @param userId 学生用户id
     * @return 学生信息
     */
    StudentBaseInfoRespVO baseInfo(Long userId);

    /**
     * 更新当前学生信息
     *
     * @param studentReqVO 请求数据
     * @throws Exception 异常信息
     */
    void updateCurrentStudent(StudentBaseReqVO studentReqVO) throws Exception;

    /**
     * 批量删除学生
     *
     * @param schoolId 学校id
     */
    void deleteBySchoolId(Long schoolId);

    /**
     * 根据班级批量删除学生
     *
     * @param clazzId 班级id
     */
    void deleteByClazzId(Long clazzId);

    /**
     * 根据班级查询学生用户id
     *
     * @param clazzId 班级id
     * @return 学生用户id列表
     */
    Set<Long> getUserIdByClazz(Long clazzId);

    /**
     * 根据用户id列表查询学生信息
     *
     * @param userIds 用户id列表
     * @return 学生基本信息
     */
    List<StudentBaseInfoDTO> getStudentListByUserIds(Set<Long> userIds);

    /**
     * 根据用户id查询学生信息
     *
     * @param userId 用户id
     * @return 学生基本信息
     */
    StudentBaseInfoDTO getStudentListByUserId(Long userId);

    /**
     * 根据姓名、学号、年级、班级、测试管理员用户id查询学生用户id列表
     *
     * @param params 查询条件
     * @return 学生用户id列表
     */
    List<Long> getUserIdsByQuery(Map<String, Object> params);

    /**
     * 分页查询学生基本信息
     *
     * @param params
     * @return
     */
    PageResult<StudentBaseRespVO> listBaseInfo(Map<String, Object> params);

    /**
     * 根据姓名查询学生用户id列表
     *
     * @param name 姓名
     * @return 学生用户id列表
     */
    List<Long> getUserIdsByName(String name);

    /**
     * 查询学生下拉列表
     *
     * @param schoolId      学校id
     * @param userName      学生名称
     * @param clazzId       班级id
     * @param idCard        身份证号
     * @param studentNumber 学号
     * @return 学生下拉列表
     */
    List<Map<String, Object>> getSelectList(Long schoolId, String userName, Long clazzId, String idCard, String studentNumber);

    /**
     * 根据条件查询学生用户id
     *
     * @param params 查询条件
     * @return 学生用户id
     */
    List<Long> getStudentUserIdByParams(Map<String, Object> params);

    /**
     * 根据学校和班级查询学生用户id和班级id
     *
     * @param schoolId 学校id
     * @param clazzIds 班级id列表
     * @return 用户id和班级id列表
     */
    List<UserClazzVO> getStudentUserIdClazzIdBySchoolAndClass(Long schoolId, List<Long> clazzIds);

    /**
     * 根据用户id查询学生基本信息
     *
     * @param userIds 用户id
     * @return 学生信息
     */
    List<StudentDTO> findStudentListByIds(Set<Long> userIds);

    /**
     * 获取学生的年级和身份证号
     *
     * @param userId 用户id
     * @return 年级和身份证号
     */
    StudentDTO findStudentGradeAndIDNumber(Long userId);

    /**
     * 根据用户id查询学生基本信息
     *
     * @param userId 用户id
     * @return 学生信息
     */
    StudentDTO findStudentInfo(Long userId);

    /**
     * 根据家长用户id查询学生用户id列表
     *
     * @param userId 家长用户id
     * @return 学生用户id列表
     */
    List<Long> findStudentUserIdByParentUserId(Long userId);
}