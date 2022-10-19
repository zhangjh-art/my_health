package com.cnasoft.health.userservice.service.impl;

import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.userservice.model.StudentFamilyCondition;
import com.cnasoft.health.userservice.service.IStudentFamilyConditionService;
import com.cnasoft.health.userservice.mapper.StudentFamilyConditionMapper;
import org.springframework.stereotype.Service;

/**
 * @author zcb
 * @description 针对表【student_family_condition(学生家庭情况表)】的数据库操作Service实现
 * @createDate 2022-03-17 17:05:34
 */
@Service
public class StudentFamilyConditionServiceImpl extends SuperServiceImpl<StudentFamilyConditionMapper, StudentFamilyCondition>
        implements IStudentFamilyConditionService {

}
