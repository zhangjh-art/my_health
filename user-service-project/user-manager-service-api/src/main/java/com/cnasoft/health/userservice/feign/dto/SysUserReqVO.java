package com.cnasoft.health.userservice.feign.dto;

import com.cnasoft.health.common.annotation.NotBlankPattern;
import com.cnasoft.health.common.util.text.TextValidator;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @author cnasoft
 * @date 2020/8/14 9:42
 */
@Data
public class SysUserReqVO {

    @ApiModelProperty(value = "姓名")
    private String name;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "头像")
    private String headImgUrl;

    @ApiModelProperty(value = "手机号")
    @NotBlankPattern(regexp = TextValidator.REGEX_MOBILE_EXACT, message = "手机号格式错误")
    private String mobile;

    @ApiModelProperty(value = "性别(1男2女)", required = true, dataType = "Integer", example = "2")
    private Integer sex;

    @ApiModelProperty(value = "邮箱")
    @NotBlankPattern(regexp = TextValidator.REGEX_EMAIL, message = "邮箱格式错误")
    private String email;

    @ApiModelProperty(value = "用户所属区域", example = "110000")
    private Integer areaCode;

    @ApiModelProperty(value = "是否启用", required = true, dataType = "Boolean", example = "true|false")
    private Boolean enabled;

    @ApiModelProperty(value = "角色编码列表")
    private List<String> roleCodes;
}
