package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 测试管理员
 *
 * @author ganghe
 * @date 2022/7/14 14:50
 **/
@Data
@TableName("test_manager")
@EqualsAndHashCode(callSuper = false)
public class TestManager extends SuperModel<TestManager> {
    /**
     * 主键id
     */
    @TableId
    private Long id;

    /**
     * 测试管理员用户id
     */
    private Long userId;

    /**
     * 学校id
     */
    private Long schoolId;

    /**
     * 审核状态 0未审核 1通过 2拒绝
     */
    private Integer approveStatus;
}
