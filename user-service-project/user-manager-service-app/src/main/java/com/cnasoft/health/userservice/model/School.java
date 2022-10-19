package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学校表
 *
 * @author lqz
 */
@Data
@TableName("school")
@EqualsAndHashCode(callSuper = false)
public class School extends SuperModel<School> {
    /**
     * 学校id
     */
    private Long id;

    /**
     * 学校名字
     */
    private String name;

    /**
     * 区域code
     */
    private Integer areaCode;

    /**
     * 审核状态
     */
    private Integer approveStatus;

    /**
     * 审核时的备注信息
     */
    @TableField(exist = false)
    private String remark;
}
