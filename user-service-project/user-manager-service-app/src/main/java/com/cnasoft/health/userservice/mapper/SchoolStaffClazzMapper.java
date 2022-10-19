package com.cnasoft.health.userservice.mapper;

import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.model.SchoolStaffClazz;
import com.cnasoft.health.userservice.model.SysUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author zcb
 */
public interface SchoolStaffClazzMapper extends SuperMapper<SchoolStaffClazz> {

    /**
     * 查询班级的班主任信息
     *
     * @param clazzIds 班级id列表
     * @param key      key
     * @return 班主任信息
     */
    List<SysUser> headteacherList(@Param("clazzIds") List<Long> clazzIds, @Param("key") String key);
}
