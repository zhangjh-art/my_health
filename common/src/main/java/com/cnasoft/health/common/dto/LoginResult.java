package com.cnasoft.health.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author cnasoft
 * @date 2021/11/19 13:30
 */
@Data
@AllArgsConstructor
public class LoginResult {

    private Integer total;

    private List<LoginUserDTO> users;
}
