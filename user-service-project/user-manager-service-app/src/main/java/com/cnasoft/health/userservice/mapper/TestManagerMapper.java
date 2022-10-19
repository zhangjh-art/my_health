package com.cnasoft.health.userservice.mapper;

import com.cnasoft.health.common.dto.SchoolDTO;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.model.TestManager;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 测试管理员
 *
 * @author ganghe
 */
public interface TestManagerMapper extends SuperMapper<TestManager> {

    /**
     * 根据用户id查询测试管理员管理的学校信息
     *
     * @param userId        用户id
     * @param approveStatus 审核状态
     * @return 学校列表
     */
    List<SchoolDTO> findSchoolList(@Param("userId") Long userId, @Param("approveStatus") Integer approveStatus);
}
