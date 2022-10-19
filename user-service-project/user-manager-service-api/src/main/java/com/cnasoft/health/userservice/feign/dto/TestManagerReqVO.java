package com.cnasoft.health.userservice.feign.dto;

import com.cnasoft.health.common.annotation.NotBlankPattern;
import com.cnasoft.health.common.dto.SchoolDTO;
import com.cnasoft.health.common.util.text.TextValidator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author ganghe
 * @date 2022/4/14 10:30
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("测试管理员请求参数")
public class TestManagerReqVO extends SysUserReqVO {

    public interface Update {
    }

    @NotNull(groups = TestManagerReqVO.Update.class, message = "用户ID不能为空")
    @ApiModelProperty(value = "用户ID")
    private Long id;

    @NotEmpty(message = "姓名不能为空")
    @ApiModelProperty(value = "姓名")
    private String name;

    @NotEmpty(message = "手机号不能为空")
    @ApiModelProperty(value = "手机号")
    @NotBlankPattern(regexp = TextValidator.REGEX_MOBILE_EXACT, message = "手机号格式错误")
    private String mobile;

    @NotNull(message = "请选择学校")
    @ApiModelProperty(value = "学校列表")
    private List<SchoolDTO> schools;
}
