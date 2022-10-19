package com.cnasoft.health.userservice.service.impl;

import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.userservice.mapper.SchoolStaffClazzMapper;
import com.cnasoft.health.userservice.model.SchoolStaffClazz;
import com.cnasoft.health.userservice.service.ISchoolStaffClazzService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.cnasoft.health.common.exception.util.ServiceExceptionUtil.exception;

/**
 * @author Administrator
 */
@Slf4j
@Service
public class SchoolStaffClazzServiceImpl extends SuperServiceImpl<SchoolStaffClazzMapper, SchoolStaffClazz> implements ISchoolStaffClazzService {


}