package com.cnasoft.health.userservice.feign.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author ganghe
 * @date 2022/7/15 10:43
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("测试管理员密码管理请求参数")
public class TestManagerPasswordReqVO extends SysUserReqVO {

    public interface Update {
    }

    public interface Check {
    }

    public interface CheckBackHome {
    }

    @NotNull(groups = TestManagerPasswordReqVO.Update.class, message = "ID不能为空")
    @ApiModelProperty(value = "ID")
    private Long id;

    @NotNull(message = "请选择学校")
    @ApiModelProperty(value = "学校id")
    private Long schoolId;

    @NotEmpty(message = "请输入密码")
    @ApiModelProperty(value = "密码")
    private String password;

    @NotEmpty(message = "请输入确认密码")
    @ApiModelProperty(value = "确认密码")
    private String confirmPassword;

    @NotNull(message = "请选择应用场景")
    @ApiModelProperty(value = "应用场景")
    private Integer applicationScene;

    @NotNull(groups = TestManagerPasswordReqVO.Check.class, message = "缺失测试管理员用户id")
    @ApiModelProperty(value = "测试管理员用户id")
    private Long testManagerUserId;

    @NotNull(groups = TestManagerPasswordReqVO.Check.class, message = "缺失应用场景")
    @ApiModelProperty(value = "应用场景")
    private Integer checkApplicationScene;

    @NotEmpty(groups = {TestManagerPasswordReqVO.Check.class, TestManagerPasswordReqVO.CheckBackHome.class}, message = "请输入密码")
    @ApiModelProperty(value = "密码")
    private String checkPassword;
}
