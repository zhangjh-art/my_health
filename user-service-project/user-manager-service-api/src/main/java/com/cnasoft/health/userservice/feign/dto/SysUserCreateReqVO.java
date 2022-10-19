package com.cnasoft.health.userservice.feign.dto;

import com.cnasoft.health.common.annotation.NotBlankPattern;
import com.cnasoft.health.common.util.text.TextValidator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

/**
 * @author cnasoft
 * @date 2020/8/14 9:42
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("新增用户请求参数")
public class SysUserCreateReqVO extends SysUserReqVO {

    @ApiModelProperty(value = "手机号", required = true)
    @NotEmpty(message = "手机号不能为空")
    @NotBlankPattern(regexp = TextValidator.REGEX_MOBILE_EXACT, message = "手机号格式错误")
    private String mobile;
}
