package com.cnasoft.health.userservice.feign.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 家长返回数据
 *
 * @author ganghe
 * @date 2022/5/25 17:12
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ParentRespVO extends UserRespVO implements Serializable {

    @ApiModelProperty(value = "激活状态")
    private Boolean isActive;

    @ApiModelProperty(value = "学生信息")
    private List<StudentRespVO> students;
}
