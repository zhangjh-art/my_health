package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author lgf
 */
@Data
@TableName("school_staff_clazz")
@EqualsAndHashCode(callSuper = false)
public class SchoolStaffClazz extends SuperModel<SchoolStaffClazz> {
    /**
     * 主键id
     */
    @TableId
    private Long id;

    /**
     * 教职工id
     */
    private Long schoolStaffId;


    /**
     * 学校id
     */
    private Long clazzId;

}
