package com.cnasoft.health.userservice.service.impl;


import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.userservice.mapper.StudentAdditionalInfoMapper;
import com.cnasoft.health.userservice.model.StudentAdditionalInfo;
import com.cnasoft.health.userservice.service.IStudentAdditionalInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * @author zcb
 */
@Service
@Slf4j
public class StudentAdditionalInfoServiceImpl extends SuperServiceImpl<StudentAdditionalInfoMapper, StudentAdditionalInfo> implements IStudentAdditionalInfoService {

}
