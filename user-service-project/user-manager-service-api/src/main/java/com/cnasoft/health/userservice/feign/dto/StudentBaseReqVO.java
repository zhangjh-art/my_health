package com.cnasoft.health.userservice.feign.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Administrator
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel("学生修改个人资料请求参数")
public class StudentBaseReqVO extends UserReqVO {
}
