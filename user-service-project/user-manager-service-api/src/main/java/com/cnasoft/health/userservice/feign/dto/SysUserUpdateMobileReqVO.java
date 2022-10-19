package com.cnasoft.health.userservice.feign.dto;

import com.cnasoft.health.common.annotation.NotBlankPattern;
import com.cnasoft.health.common.util.text.TextValidator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author cnasoft
 * @date 2020/8/14 9:42
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("修改用户请求参数")
public class SysUserUpdateMobileReqVO extends SysUserReqVO {

    @ApiModelProperty(value = "新手机号")
    @NotBlankPattern(regexp = TextValidator.REGEX_MOBILE_EXACT, message = "新手机号格式错误")
    private String newMobile;

    @ApiModelProperty(value = "验证码")
    private String captcha;
}
