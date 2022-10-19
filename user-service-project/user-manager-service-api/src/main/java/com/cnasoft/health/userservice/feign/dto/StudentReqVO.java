package com.cnasoft.health.userservice.feign.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zcb
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("查询学生列表请求参数")
public class StudentReqVO implements Serializable {

    @ApiModelProperty(value = "混合输入框")
    private String mixInput;

    @ApiModelProperty(value = "年级")
    private String grade;

    @ApiModelProperty(value = "班级")
    private Long clazzId;

    @ApiModelProperty(value = "启用状态: 0: 否, 1: 是")
    private Byte enabled;

    private Long schoolId;

    private Integer pageNum;
    private Integer pageSize;

}
