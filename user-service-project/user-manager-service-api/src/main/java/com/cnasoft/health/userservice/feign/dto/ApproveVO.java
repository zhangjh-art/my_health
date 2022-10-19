package com.cnasoft.health.userservice.feign.dto;

import com.cnasoft.health.common.util.io.serializer.DateSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 审核列表数据VO
 *
 * @Created by lgf on 2022/4/21.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApproveVO {

    @ApiModelProperty(value = "id")
    private String id;

    @ApiModelProperty(value = "审核数据类型")
    private Integer approveType;

    @ApiModelProperty(value = "审核操作类型")
    private Integer approveOperation;

    @ApiModelProperty(value = "原数据json")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String beforeJson;

    @ApiModelProperty(value = "修改后json")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String afterJson;

    @ApiModelProperty(value = "审核状态 0新增1修改2删除3启用4禁用")
    private Integer approveStatus;

    @ApiModelProperty(value = "申请人用户id")
    private String applicantUserId;

    @ApiModelProperty(value = "申请人名称")
    private String applicantName;

    @JsonSerialize(using = DateSerializer.class)
    @ApiModelProperty(value = "申请日期")
    private Date applicantDate;

    @ApiModelProperty(value = "备注")
    private String remark;
}
