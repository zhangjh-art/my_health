package com.cnasoft.health.userservice.feign.dto;

import lombok.Data;

/**
 * @author ganghe
 * @date 2022/5/15 14:14
 **/
@Data
public class IntelligentImportVO {
    /**
     * 当前用户id
     */
    private Long userId;

    /**
     * 当前登录人角色
     */
    private String roleCode;

    /**
     * 学校id
     */
    private Long schoolId;

    /**
     * 区域编码
     */
    private Integer areaCode;

    /**
     * 导入类型：1：学生；2：家长；3：校教职工；4：校心理老师；5：区域职员；6：区域心理教研员
     */
    private Integer importType;

    /**
     * 列标题与实体属性的映射关系
     */
    private String columnMapping;

    /**
     * 是否覆盖数据
     */
    private Boolean cover;
}
