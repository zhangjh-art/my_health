package com.cnasoft.health.userservice.service;

import com.cnasoft.health.common.dto.ApproveDTO;
import com.cnasoft.health.common.dto.ApproveSimpleDTO;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.enums.ApproveOperation;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.enums.ApproveType;
import com.cnasoft.health.common.service.ISuperService;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.feign.dto.ApplicantUserInfoVO;
import com.cnasoft.health.userservice.feign.dto.ApproveVO;
import com.cnasoft.health.userservice.model.Approve;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @description 针对表【approve】的数据库操作Service
 * @createDate 2022-04-13 11:04:57
 */
public interface IApproveService extends ISuperService<Approve> {

    /**
     * 批量插入审核记录
     *
     * @param approves 审核数据
     * @return
     */
    boolean insertBatch(List<ApproveDTO> approves);

    /**
     * 分页查询审核记录
     *
     * @param params 查询条件
     * @return
     */
    PageResult<ApproveVO> listByParam(Map<String, Object> params);

    /**
     * 新增审核记录
     *
     * @param type
     * @param operation
     * @param status
     * @param businessId
     * @param applicantUserId
     * @param beforeJson
     * @param afterJson
     */
    void addApproveRecord(ApproveType type, ApproveOperation operation, ApproveStatus status, Long businessId, Long applicantUserId, String beforeJson, String afterJson);

    /**
     * 新增审核记录
     *
     * @param type
     * @param operation
     * @param status
     * @param businessId
     * @param applicantUserId
     */
    void addApproveRecord(ApproveType type, ApproveOperation operation, ApproveStatus status, Long businessId, Long applicantUserId);

    /**
     * 处理审核通过
     *
     * @param approveRecordId
     * @param allow
     * @param rejectReason
     * @return
     */
    BatchOperationTipDTO handleApprove(Long approveRecordId, boolean allow, String rejectReason);

    /**
     * 获取申请人列表
     *
     * @param type
     * @return
     */
    List<ApplicantUserInfoVO> getApplicantUserList(String type);

    /**
     * 查询资源审批数量
     *
     * @param approveType
     * @param operation
     * @param approveStatus
     * @param resourceIds
     * @return
     */
    CommonResult<Integer> selectApproveCount(Integer approveType, Integer operation, Integer approveStatus, List<Long> resourceIds);

    /**
     * 获取最新的审核记录信息列表
     *
     * @param approveType 审核数据类型
     * @param businessIds 审核数据id列表
     * @return boolean
     */
    List<ApproveSimpleDTO> getApproveList(Integer approveType, List<Long> businessIds);

    /**
     * 查询审核数据
     *
     * @param approveType 审核数据类型
     * @param businessId  审核数据id
     * @return
     */
    Approve getApprove(Integer approveType, Long businessId);
}
