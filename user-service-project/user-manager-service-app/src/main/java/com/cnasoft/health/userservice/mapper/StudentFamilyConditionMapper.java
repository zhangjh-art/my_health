package com.cnasoft.health.userservice.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.enums.FamilyCondition;
import com.cnasoft.health.userservice.model.StudentFamilyCondition;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author zcb
* @description 针对表【student_family_condition(学生家庭情况表)】的数据库操作Mapper
* @createDate 2022-03-17 17:05:34
* @Entity com.cnasoft.health.userservice.model.StudentFamilyCondition
*/
@DS(Constant.DATA_SOURCE_MYSQL)
public interface StudentFamilyConditionMapper extends SuperMapper<StudentFamilyCondition> {

    /**
     * 批量插入学生家庭信息
     * @param familyConditions
     */
    void saveBatch(@Param("list") List<StudentFamilyCondition> familyConditions);
}
