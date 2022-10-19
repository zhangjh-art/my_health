package com.cnasoft.health.userservice.feign.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * @author zcb
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("家长绑定学生请求参数")
public class ParentBindStudentVO implements Serializable {

    /**
     * 参数分组验证
     */
    public interface Mobile {
    }

    @ApiModelProperty(value = "手机号，移动端绑定时需要")
    @NotBlank(groups = Mobile.class, message = "手机号不能为空")
    private String m;

    @ApiModelProperty(value = "验证码，移动端绑定时需要")
    @NotBlank(groups = Mobile.class, message = "验证码不能为空")
    private String v;

    @ApiModelProperty(value = "学生集合")
    @NotNull(message = "学生不能为空")
    @Size(min = 1, max = 10, message = "学生个数1到10之间")
    @Valid
    private List<BindStudentVO> students;
}