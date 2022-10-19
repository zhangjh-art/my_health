package com.cnasoft.health.userservice.feign.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author ganghe
 * @date 2022/5/11 12:11
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("学校新增/编辑请求参数")
public class SchoolReqVO {

    public interface Update {
    }

    @NotNull(groups = SchoolReqVO.Update.class, message = "id不能为空")
    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "学校名")
    @NotEmpty(message = "学校名称不能为空")
    private String name;

    @ApiModelProperty(value = "区域编码")
    @NotNull(message = "区域不能为空")
    private Integer areaCode;
}
