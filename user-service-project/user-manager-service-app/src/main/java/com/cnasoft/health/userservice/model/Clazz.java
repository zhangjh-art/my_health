package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author lqz
 * @TableName class
 * 班级表
 */
@Data
@TableName("clazz")
@EqualsAndHashCode(callSuper = false)
public class Clazz extends SuperModel<Clazz> {
    /**
     * 班级id
     */
    @TableId
    private Long id;

    /**
     * 年级编码
     */
    private String grade;

    /**
     * 班级名字
     */
    private String clazzName;

    /**
     * 学校id
     */
    private Long schoolId;

    /**
     * 入学时间（年份）
     */
    private Integer admissionDate;

    /**
     * 是否已毕业：1、是；0、否
     */
    private Boolean isGraduated;
}