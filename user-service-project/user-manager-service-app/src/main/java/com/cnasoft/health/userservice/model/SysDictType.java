package com.cnasoft.health.userservice.model;

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
@TableName("sys_dict_type")
public class SysDictType extends SuperModel<SysDictType> {
    /**
     * 数据字典类型id
     */
    @TableId
    private Long id;

    /**
     * 类型，如：sex,department,grade
     */
    private String dictTypeCode;

    /**
     * 类型值，如：性别，部门，年级
     */
    private String dictTypeName;

    /**
     * 字典类型描述
     */
    private String dictDesc;

    public SysDictType(String dictTypeCode, String dictTypeName) {
        this.dictTypeCode = dictTypeCode;
        this.dictTypeName = dictTypeName;
    }
}
