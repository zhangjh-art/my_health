package com.cnasoft.health.userservice.feign.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * @Created by lgf on 2022/4/6.
 */
@Data
public class FeedbackVO {

    @NotNull(message = "确认密码不能为空")
    @ApiModelProperty("反馈内容")
    private String content;

    @ApiModelProperty("提交的图片链接")
    private List<String> uploadImageUrl;

    @NotNull(message = "联系电话不能为空")
    @ApiModelProperty("联系电话")
    private String mobile;
}
