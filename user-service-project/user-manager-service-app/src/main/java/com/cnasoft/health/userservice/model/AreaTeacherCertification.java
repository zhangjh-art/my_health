package com.cnasoft.health.userservice.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Created by lqz on 2022/4/15.
 * 区域心理教研员的资质文件存储地址
 *
 */
@Data
@TableName("area_teacher_certification")
@EqualsAndHashCode(callSuper = false)
public class AreaTeacherCertification extends SuperModel<AreaTeacherCertification> {
    /**
     * 主键id
     */
    @TableId
    private Long id;

    /**
     * 区域心理教研员id
     */
    private Long areaTeacherId;
    /**
     * 资质文件地址
     */
    private String qualificationFile;
}
