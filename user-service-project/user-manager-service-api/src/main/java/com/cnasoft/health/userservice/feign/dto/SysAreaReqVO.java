package com.cnasoft.health.userservice.feign.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author ganghe
 * @date 2022/4/9 17:16
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("区域新增请求参数")
public class SysAreaReqVO {

    public interface Update {
    }

    @NotNull(groups = SysAreaReqVO.Update.class, message = "区域ID不能为空")
    @ApiModelProperty(value = "区域ID")
    private Long id;

    @NotBlank(message = "区域名称不能为空")
    @ApiModelProperty(value = "区域名称")
    private String name;

    @NotBlank(message = "区域编码不能为空")
    @ApiModelProperty(value = "区域编码")
    private String code;
}
