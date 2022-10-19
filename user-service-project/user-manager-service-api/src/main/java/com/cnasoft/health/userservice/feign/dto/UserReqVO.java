package com.cnasoft.health.userservice.feign.dto;

import com.cnasoft.health.common.annotation.NotBlankPattern;
import com.cnasoft.health.common.util.text.TextValidator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import com.cnasoft.health.common.annotation.NotBlankLength;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author ganghe
 * @date 2022/5/11 0:39
 **/
@Data
@ApiModel("用户公共数据")
public class UserReqVO {

    public interface Add {
    }

    public interface Update {
    }

    @NotNull(groups = UserReqVO.Update.class, message = "id不能为空")
    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "姓名，长度限制2~20位")
    @NotEmpty(message = "姓名不能为空", groups = {UserReqVO.Add.class})
    @NotBlankLength(max = 20, min = 2, message = "姓名长度限制2~20位")
    private String name;

    @ApiModelProperty(value = "性别: 1: 男，2: 女")
    private Integer sex;

    @ApiModelProperty(value = "邮箱")
    @NotBlankPattern(regexp = TextValidator.REGEX_EMAIL, message = "邮箱格式错误")
    private String email;

    @ApiModelProperty(value = "手机号", required = true)
    @NotEmpty(message = "手机号不能为空", groups = {UserReqVO.Add.class})
    @NotBlankPattern(regexp = TextValidator.REGEX_MOBILE_EXACT, message = "手机号格式错误")
    private String mobile;

    @ApiModelProperty(value = "头像")
    private String headImgUrl;

    @ApiModelProperty(value = "昵称，长度限制20位")
    @NotBlankLength(max = 20, message = "昵称长度限制20位")
    private String nickname;

    @ApiModelProperty(value = "验证码")
    private String verifyCode;
}
