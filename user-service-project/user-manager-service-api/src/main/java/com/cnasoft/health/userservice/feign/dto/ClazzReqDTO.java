package com.cnasoft.health.userservice.feign.dto;

import com.cnasoft.health.common.model.CommonModel;
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
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("班级新增/编辑请求数据")
public class ClazzReqDTO {

    public interface Update {
    }

    @NotNull(groups = ClazzReqDTO.Update.class, message = "id不能为空")
    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "年级编码")
    @NotEmpty(message = "年级不能为空")
    private String grade;

    @ApiModelProperty(value = "班级名")
    @NotEmpty(message = "班级名不能为空")
    private String clazzName;

    @ApiModelProperty(value = "入学时间（年份）")
    private Integer admissionDate;

    @ApiModelProperty(value = "是否已毕业")
    private Boolean isGraduated;

    @ApiModelProperty(value = "班主任id集合，新增、编辑时用")
    private List<Long> headerTeacherIds;

    @ApiModelProperty(value = "班主任集合，列表查询时返回")
    private List<CommonModel> headerTeachers;
}
