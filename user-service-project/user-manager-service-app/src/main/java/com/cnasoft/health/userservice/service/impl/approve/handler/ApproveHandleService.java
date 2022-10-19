package com.cnasoft.health.userservice.service.impl.approve.handler;

import com.cnasoft.health.userservice.feign.dto.ApproveVO;
import com.cnasoft.health.userservice.model.Approve;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */
public interface ApproveHandleService {

    /**
     * 新增数据的审核处理方法
     *
     * @param approve 审核记录
     * @param allow   是否审核通过
     * @throws Exception 异常信息
     */
    void handleAddApprove(Approve approve, boolean allow) throws Exception;

    /**
     * 修改数据时审核处理方法
     *
     * @param approve 审核记录
     * @throws Exception 异常信息
     */
    void handleUpdateApprove(Approve approve) throws Exception;

    /**
     * 删除数据时审核处理方法
     *
     * @param approve 审核记录
     */
    void handleDeleteApprove(Approve approve);

    /**
     * 启用/禁用时审核处理方法
     *
     * @param approve 审核记录
     */
    default void handleEnableApprove(Approve approve) {
    }

    /**
     * 查询审核业务id列表
     *
     * @param params 查询条件
     * @return id列表
     */
    default List<Long> queryApproveBusinessId(Map<String, Object> params) {
        return null;
    }

    /**
     * 处理审核记录中修改前和修改后的数据
     *
     * @param approve 审核记录
     */
    default void handleQueryResult(ApproveVO approve) {
    }
}
