package com.cnasoft.health.userservice.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.userservice.feign.dto.StudentApplicationDto;
import com.cnasoft.health.userservice.mapper.StudentApplicationMapper;
import com.cnasoft.health.userservice.model.StudentApplication;
import com.cnasoft.health.userservice.service.IStudentApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class StudentApplicationServiceImpl extends SuperServiceImpl<StudentApplicationMapper, StudentApplication> implements IStudentApplicationService {


    @Resource
    StudentApplicationMapper studentApplicationMapper;

    @Override
    public List<StudentApplicationDto> queryStudentApplicationList(Long userId) {
        List<StudentApplicationDto> studentApplicationList = studentApplicationMapper.selectStudentApplicationList(userId);
        return studentApplicationList;
    }
}
