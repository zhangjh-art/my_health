package com.cnasoft.health.userservice.feign.dto;

import com.cnasoft.health.common.annotation.NotBlankLength;
import com.cnasoft.health.common.annotation.NotBlankPattern;
import com.cnasoft.health.common.util.text.TextValidator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author zcb
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("添加/编辑家长请求参数")
public class ParentReqVO implements Serializable {
    public interface Add {
    }

    public interface Update {
    }

    @ApiModelProperty(value = "家长id，编辑时用")
    @NotNull(groups = ParentReqVO.Update.class, message = "家长id不能为空")
    private Long id;

    @ApiModelProperty(value = "家长姓名")
    @NotNull(groups = ParentReqVO.Add.class, message = "姓名不能为空")
    @NotBlankLength(max = 20, min = 2, message = "姓名长度限制2~20位")
    private String name;

    @ApiModelProperty(value = "昵称，长度限制20位")
    @NotBlankLength(max = 20, message = "昵称长度限制20位")
    private String nickname;

    @ApiModelProperty(value = "学生id")
    private List<StudentVO> students;

    @ApiModelProperty(value = "性别: 1: 男 2: 女")
    @NotNull(message = "性别不能为空", groups = ParentReqVO.Add.class)
    private Integer sex;

    @ApiModelProperty(value = "邮箱")
    @NotBlankPattern(regexp = TextValidator.REGEX_EMAIL, message = "邮箱格式错误")
    private String email;

    @ApiModelProperty(value = "手机")
    @NotNull(message = "手机号不能为空", groups = ParentReqVO.Add.class)
    @NotBlankPattern(regexp = TextValidator.REGEX_MOBILE_EXACT, message = "手机号格式错误")
    private String mobile;

    @ApiModelProperty(value = "验证码")
    private String verifyCode;
}
