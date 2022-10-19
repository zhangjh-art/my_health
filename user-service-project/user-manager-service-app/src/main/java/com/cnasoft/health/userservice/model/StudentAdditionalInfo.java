package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学生补充信息
 *
 * @author zcb
 * @TableName student_additional_info
 */
@Data
@TableName("student_additional_info")
@EqualsAndHashCode(callSuper = false)
public class StudentAdditionalInfo extends SuperModel<StudentAdditionalInfo> {
    /**
     * 主键id
     */
    @TableId
    private Long id;

    /**
     * 学生id
     */
    private Long studentId;

    /**
     * 国籍
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer country;

    /**
     * 港澳台侨外，1：是；0：否
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean gatqw;

    /**
     * 民族，1：汉族；2：彝族；3：回族；4：藏族；5：蒙古族；6：满族；7：维吾尔族；8：苗族；9：壮族；10：土家族；11：其他
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String nation;

    /**
     * 政治面貌
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer politicsStatus;

    /**
     * 籍贯
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String nativePlace;

    /**
     * 出生地
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String birthPlace;

    /**
     * 家中排行
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer familySort;

    /**
     * 血型
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer bloodType;

    /**
     * 健康状况
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer healthyStatus;

    /**
     * 疾病史
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String illnessHistory;

    /**
     * 是否孤儿，1：是；0：否
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isOrphan;

    /**
     * 是否留守儿童，1：非留守儿童；2：单亲留守儿童；3：双亲留守儿童
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer isLeft;

    /**
     * 是否独生子女，1：是；0：否
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isOnly;

    /**
     * 残疾类型
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer disabilityType;

    /**
     * 是否是烈士或优抚子女，1：是；0：否
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isChildOfMartyrEntitled;

    /**
     * 是否进城务工随迁，1：是；0：否
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isChildOfMigrant;

    /**
     * 是否申请资助，1：是；0：否
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isSubsidized;

    /**
     * 身高，单位：cm
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String height;

    /**
     * 体重，单位kg
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String weight;

    /**
     * 特俗情况分类：1：辍学:；2：复读；3：贫困生；4：家庭情况特殊；5：其他
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer specialCondition;
}