package com.cnasoft.health.userservice.feign.dto;

import com.cnasoft.health.common.util.io.serializer.DateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class NewReservationRespVO {

    private Long id;

    private Integer status;

    private Long psychiatristId;

    private String psychiatristName;

    private Long userId;

    private String userName;

    private String department;

    @JsonSerialize(using = DateSerializer.class)
    private Date birthday;

    private String post;

    private String presetRoleCode;

    private String headImgUrl;

    private String grade;

    private Long clazzId;

    private Integer sex;

    private Integer isEarlyWarning;

    private List<StudentRespVO> studentInfo;

    private Integer isSubstituted;

    private Integer userRoleType;

    @JsonSerialize(using = DateSerializer.class)
    private Date date;

    private String startTime;

    private String endTime;

    @JsonSerialize(using = DateSerializer.class)
    private Date createTime;

    private String consultTypes;

    private String consultTypeNames;

    private String consultDescription;

    private String description;

    private String remark;

    private String cancelOtherReason;

    private String question1;

    private String question2;

    private String question3;

    private String question4;

    private String question5;
}
