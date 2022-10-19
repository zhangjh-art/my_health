package com.cnasoft.health.userservice.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.feign.dto.StudentApplicationDto;
import com.cnasoft.health.userservice.model.StudentApplication;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@DS(Constant.DATA_SOURCE_MYSQL)
public interface StudentApplicationMapper extends SuperMapper<StudentApplication> {

    List<StudentApplicationDto> selectStudentApplicationList(@Param("userId") Long userId);
}
