package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学生家庭情况表
 *
 * @author zcb
 * @TableName student_family_condition
 */
@Data
@TableName("student_family_condition")
@EqualsAndHashCode(callSuper = false)
public class StudentFamilyCondition extends SuperModel<StudentFamilyCondition> {

    private Long id;

    /**
     * 学生表id
     */
    private Long studentId;

    /**
     * 家庭情况，1：正常；2：单亲；3：离异再婚；4：丧亡学生；5：非婚子女；6：父母服刑7：吸毒人员家庭子女
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer familyCondition;
}