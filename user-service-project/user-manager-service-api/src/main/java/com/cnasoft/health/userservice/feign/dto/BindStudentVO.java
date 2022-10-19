package com.cnasoft.health.userservice.feign.dto;

import com.cnasoft.health.common.util.text.TextValidator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * @author zcb
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("家长绑定学生时学生请求参数")
public class BindStudentVO implements Serializable {

    @ApiModelProperty(value = "学生姓名，必填")
    @NotBlank(message = "学生姓名不能为空")
    private String name;

    @ApiModelProperty(value = "学生身份证,必填")
    @Pattern(regexp = TextValidator.REGEX_ID, message = "身份证格式不正确")
    private String identityCardNumber;

    @ApiModelProperty(value = "家长关系，必填")
    @NotNull(message = "家长关系不能为空")
    private Integer relationship;
}