package com.cnasoft.health.userservice.feign.dto;

import com.cnasoft.health.common.annotation.NotBlankPattern;
import com.cnasoft.health.common.enums.Sex;
import com.cnasoft.health.common.util.text.TextValidator;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Classname BaseViewObject
 * @Description 基类
 * @Date 2022/4/14 10:47
 * @Created by Shadow
 */
@Data
public class BaseViewObject {

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
    private Sex sex;

    @ApiModelProperty(value = "邮箱")
    @NotBlankPattern(regexp = "^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(.[a-zA-Z0-9-]+)*.[a-zA-Z0-9]{2,6}$", message = "邮箱格式错误")
    private String email;

    @ApiModelProperty(value = "用户所属区域", example = "110000")
    private Integer areaCode;

    @ApiModelProperty(value = "是否启用", required = true, dataType = "Boolean", example = "true|false")
    private Boolean enabled;

    @ApiModelProperty(value = "角色id列表", example = "1,3,4,5,6,7")
    private String roleId;
}
