package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 学生基本信息实体
 *
 * @author zcb
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("student_base_info")
public class StudentBaseInfo extends SuperModel<StudentBaseInfo> {
    @TableId
    private Long id;

    /**
     * 用户表主键id
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户姓名
     */
    private String name;

    /**
     * 性别: 1: 男，2: 女
     */
    private Integer sex;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 启用状态: 0: 否, 1: 是
     */
    private Boolean enabled;

    /**
     * 曾用名
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String usedName;

    /**
     * 学号
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String studentNumber;

    /**
     * 学籍号
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String studentCode;

    /**
     * 学籍状态：1=在读，2=毕业，3=转校，4=休学，5=退学，6=肄业
     */
    private Integer studentStatus;

    /**
     * 学校id
     */
    private Long schoolId;

    /**
     * 年级
     */
    private String grade;

    /**
     * 班级id
     */
    private Long clazzId;

    /**
     * 入学年份
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer admissionYear;

    /**
     * 身份证号
     */
    private String identityCardNumber;

    /**
     * 家长id
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long parentId;

    /**
     * 家长关系
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer relationship;

    /**
     * 生日
     */
    private String birthday;

    /**
     * 模板id，0表示手动添加
     */
    private Long templateId;

    @TableField(exist = false)
    private Long studentAdditionalInfoId;

    @TableField(exist = false)
    private Long studentFamilyConditionId;

    /**
     * 头像
     */
    @TableField(exist = false)
    private String headImgUrl;

    @TableField(exist = false)
    private String nickname;

    @TableField(exist = false)
    private List<Long> studentFamilyConditionIds;
}
