package com.cnasoft.health.userservice.feign.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author zcb
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("添加/编辑家长时学生请求参数")
public class StudentVO implements Serializable {
    public interface Update {
    }

    @ApiModelProperty(value = "学生用户id，编辑学生个人资料时用")
    @NotNull(groups = StudentVO.Update.class, message = "学生id不能为空")
    private Long id;

    @ApiModelProperty(value = "学生用户id")
    @NotNull(groups = StudentVO.Update.class, message = "学生用户id不能为空")
    private Long userId;

    @ApiModelProperty(value = "学生身份证")
    private String identityCardNumber;

    @ApiModelProperty(value = "家长关系")
    private Integer relationship;

    @ApiModelProperty(value = "名称")
    private String name;
}
