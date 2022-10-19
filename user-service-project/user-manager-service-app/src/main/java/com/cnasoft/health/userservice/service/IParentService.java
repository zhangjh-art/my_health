package com.cnasoft.health.userservice.service;

import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.ParentDTO;
import com.cnasoft.health.common.dto.ParentStudentDTO;
import com.cnasoft.health.common.service.ISuperService;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.feign.dto.BindStudentVO;
import com.cnasoft.health.userservice.feign.dto.ParentBaseRespVO;
import com.cnasoft.health.userservice.feign.dto.ParentBindStudentVO;
import com.cnasoft.health.userservice.feign.dto.ParentReqVO;
import com.cnasoft.health.userservice.feign.dto.ParentRespVO;
import com.cnasoft.health.userservice.model.Parent;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zcb
 * @description 针对表【parent(家长表)】的数据库操作Service
 * @createDate 2022-03-24
 */
public interface IParentService extends ISuperService<Parent> {

    /**
     * 新增家长
     *
     * @param parent 请求数据
     * @return 结果
     * @throws Exception 异常
     */
    List<String> create(ParentReqVO parent) throws Exception;

    /**
     * 修改家长
     *
     * @param parent 请求数据
     * @return 结果
     * @throws Exception 异常
     */
    List<String> updateParent(ParentReqVO parent) throws Exception;

    /**
     * 删除指定家长
     *
     * @param ids id列表
     * @return 错误列表
     */
    List<BatchOperationTipDTO> delete(Set<Long> ids);

    /**
     * 家长列表
     *
     * @param params 查询条件
     * @return 家长列表
     */
    PageResult<ParentRespVO> list(Map<String, Object> params);

    /**
     * pc端绑定学生
     *
     * @param query 绑定条件
     * @return 错误数据
     */
    List<String> pcBindStudents(ParentBindStudentVO query);

    /**
     * 移动端家长绑定学生
     *
     * @param query 绑定条件
     * @return 错误数据
     */
    List<String> mobileBindStudents(ParentBindStudentVO query);

    /**
     * 更新家长确认状态及激活状态
     *
     * @param userId 用户id
     */
    void updateConfirmAndActiveStatus(Long userId);

    /**
     * 根据用户id获取家长详情
     *
     * @param userId 用户id
     * @return 家长信息
     */
    ParentRespVO infoByUserId(Long userId);

    /**
     * 根据用户id获取家长详情
     *
     * @param userId 用户id
     * @return 家长信息
     */
    ParentRespVO infoOnlyByUserId(Long userId);

    /**
     * 更新当前登录家长信息
     *
     * @param parentReqVO 家长信息
     */
    void updateCurrentParent(ParentReqVO parentReqVO) throws Exception;

    /**
     * h5端绑定学生
     *
     * @param vo
     */
    List<String> h5BindStudent(BindStudentVO vo);

    /**
     * 根据学校id删除
     *
     * @param schoolId 学校id
     */
    void deleteBySchoolId(Long schoolId);

    /**
     * 家长下拉列表
     *
     * @param schoolId 查询条件
     * @param userName 家长名称
     * @return 家长列表
     */
    List<Map<String, Object>> getSelectList(Long schoolId, String userName);

    /**
     * 分页获取家长基础信息
     *
     * @param params
     * @return
     */
    PageResult<ParentBaseRespVO> listBaseInfo(Map<String, Object> params);

    /**
     * 根据条件查询查询家长用户id
     *
     * @param params 查询条件
     * @return 家长用户id列表
     */
    List<Long> getParentUserIdByParams(Map<String, Object> params);

    /**
     * 根据用户id查询家长基本信息及子女信息
     *
     * @param userId 用户id
     * @return
     */
    ParentStudentDTO findParentInfo(Long userId);

    /**
     * 根据用户id列表查询家长基本信息及子女姓名
     *
     * @param userIds 用户id列表
     * @return 家长信息
     */
    List<ParentDTO> findParentList(Set<Long> userIds);
}
