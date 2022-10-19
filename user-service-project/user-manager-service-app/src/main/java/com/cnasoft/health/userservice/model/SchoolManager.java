package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 校级管理员
 * @author ganghe
 * @date 2022/4/14 10:24
 **/
@Data
@TableName("school_manager")
@EqualsAndHashCode(callSuper = false)
public class SchoolManager extends SuperModel<SchoolManager> {
    /**
     * 主键id
     */
    @TableId
    private Long id;

    /**
     * 学校id
     */
    private Long schoolId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 审核状态 0未审核 1通过 2拒绝
     */
    private Integer approveStatus;
}
