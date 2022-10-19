package com.cnasoft.health.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @author cnasoft
 * @date 2021/11/22 10:18
 */
@Data
@Getter
@Setter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginUserDTO {

    private Long userId;

    private String roleCode;

    private String areaName;

    private String schoolName;
}
