package com.cnasoft.health.userservice.service;

import com.cnasoft.health.common.service.ISuperService;
import com.cnasoft.health.userservice.feign.dto.StudentApplicationDto;
import com.cnasoft.health.userservice.model.StudentApplication;

import java.util.List;

public interface IStudentApplicationService extends ISuperService<StudentApplication> {

    /**
     * 查询所有应用
     *
     * @return
     */
    List<StudentApplicationDto> queryStudentApplicationList(Long userId);
}
