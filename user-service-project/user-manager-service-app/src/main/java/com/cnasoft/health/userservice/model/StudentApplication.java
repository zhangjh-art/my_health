package com.cnasoft.health.userservice.model;


import com.baomidou.mybatisplus.annotation.TableName;
import com.cnasoft.health.db.autoconfigure.dataobject.SuperModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("student_application")
@EqualsAndHashCode(callSuper = false)
public class StudentApplication extends SuperModel<StudentApplication> {

    private int id;//id

    private Long userId;//用户id

    private String applicationName;// 应用名称

    private String applicationImg;// 应用图片

    private String applicationDesc;// 应用描述

    private String applicationUrl;// 应用描述

}
