package com.cnasoft.health.userservice.feign;

import com.cnasoft.health.common.constant.ServiceNameConstants;
import com.cnasoft.health.common.dto.ApproveDTO;
import com.cnasoft.health.common.dto.ApproveSimpleDTO;
import com.cnasoft.health.common.vo.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 审核模块学校feign接口
 *
 * @author zcb
 */
@FeignClient(name = ServiceNameConstants.USER_SERVICE, decode404 = true)
public interface ApproveFeignClient {

    /**
     * 批量插入审核记录
     *
     * @param approveDTOList 审批数据
     * @return 学生的用户id列表
     */
    @PostMapping(value = "/approve/insertBatch")
    CommonResult<Object> insertBatch(@RequestBody List<ApproveDTO> approveDTOList);

    /**
     * 查询资源通过审批的数量
     *
     * @param approveType   审核类型，1：工作权限审核；2：角色审核；3：区域审核；4：学校审核；5：部门审核；6：量表审核；7：账号审核；8：资源审核
     * @param operation     操作类型，1：新增；2：编辑；3：删除
     * @param approveStatus 审核状态，0：待审核；1：通过；2：拒绝
     * @param resourceIds   资源id
     * @return Integer
     */
    @PostMapping(value = "/approve/select-approve-count")
    CommonResult<Integer> selectApproveCount(@RequestParam("approveType") Integer approveType, @RequestParam("operation") Integer operation,
        @RequestParam("approveStatus") Integer approveStatus, @RequestParam("resourceIds") List<Long> resourceIds);

    /**
     * 通过审核
     *
     * @param recordId recordId
     * @return null
     */
    @GetMapping("/approve/allow")
    CommonResult<Object> allow(@RequestParam("id") Long recordId);

    /**
     * 拒绝审核申请
     *
     * @param params params
     * @return null
     */
    @PostMapping("/approve/reject")
    CommonResult<Object> reject(@RequestBody Map<String, Object> params);

    /**
     * 获取最新的审核记录信息列表
     *
     * @param approveType 审核类型
     * @param businessIds 业务id列表
     * @return
     */
    @GetMapping(value = "/approve/approveList")
    CommonResult<List<ApproveSimpleDTO>> getApproveList(@RequestParam("approveType") Integer approveType, @RequestParam("businessIds") List<Long> businessIds);
}
