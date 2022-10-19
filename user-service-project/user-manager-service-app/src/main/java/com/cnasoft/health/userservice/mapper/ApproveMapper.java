package com.cnasoft.health.userservice.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.dto.ApproveSimpleDTO;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.feign.dto.ApplicantUserInfoVO;
import com.cnasoft.health.userservice.model.Approve;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @description 针对表【approve】的数据库操作Mapper
 * @createDate 2022-04-13 11:04:57
 * @Entity com.cnasoft.health.userservice.model.Approve
 */
public interface ApproveMapper extends SuperMapper<Approve> {

    List<Approve> listByParam(Page<Approve> page, @Param("u") Map<String, Object> params);

    List<ApplicantUserInfoVO> getApplicantUserList(@Param("type") String type, @Param("key") String key);

    /**
     * 获取最新的审核记录信息列表
     *
     * @param approveType 审核类型
     * @param businessIds 业务id列表
     * @return
     */
    List<ApproveSimpleDTO> getApproveList(@Param("approveType") Integer approveType, @Param("businessIds") List<Long> businessIds);
}
