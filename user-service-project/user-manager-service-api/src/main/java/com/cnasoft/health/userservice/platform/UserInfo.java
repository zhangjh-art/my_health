package com.cnasoft.health.userservice.platform;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserInfo {

    public static final String ROLE_STUDENT = "学生";
    public static final String ROLE_PARENT = "家长";
    public static final String ROLE_STAFF = "教职工";
    public static final String ROLE_HEAD_TEACHER = "班主任";
    public static final String ROLE_PSYCHO_TEACHER = "心理教师";
    public static final String ROLE_SCHOOL_LEADER = "校领导";

    public UserInfo() {
        this.evaluate = new ArrayList<>();
    }

    /**
     * 唯一标识,必填,学生身份证sha1加密，其他角色手机号sha1加密
     */
    private String uid;
    /**
     * 姓名
     */
    private String name;
    /**
     * 学生|教师|家长|心理教师，必填
     */
    private String role;
    /**
     * 学生|教师|家长|心理教师，必填
     */
    private String roleCode;
    /**
     * 学校名称或其它机构
     */
    private String org;
    /**
     * 学生年级
     */
    private String grade;
    /**
     * 身份证
     */
    private String idno;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 评测结果
     */
    private List<EvaluateResInfo> evaluate;

}
