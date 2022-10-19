package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author lqz
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_dict_data")
public class SysDictData extends SuperModel<SysDictData> {
    /**
     * 数据字典id
     */
    @TableId
    private Long id;

    /**
     * 字典类型code:根据该code查询数据
     */
    private String dictType;

    /**
     * 字段类型名称
     */
    @TableField(exist = false)
    private String dictTypeName;

    /**
     * 字典数据展示值
     */
    private String dictName;

    /**
     * 字典数据code：下拉框实现获取和数据库实际存储的值
     */
    private String dictValue;

    /**
     * 字典数据描述
     */
    private String dictDesc;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 字典数据状态：false启用，true禁用
     */
    private Boolean disable;

    /**
     * 是否内置：false没有内置 true内置
     */
    private Boolean preset;

    /**
     * 审核状态 0未审核 1已通过 2已拒绝
     */
    private Integer approveStatus;

    /**
     * 审核时的备注信息
     */
    @TableField(exist = false)
    private String remark;
}
