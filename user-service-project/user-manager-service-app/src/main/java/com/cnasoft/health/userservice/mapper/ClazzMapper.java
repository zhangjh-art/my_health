package com.cnasoft.health.userservice.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.dto.ClazzDTO;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.model.Clazz;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lqz
 * 针对表【class】的数据库操作Mapper
 * 2022-03-24
 */
@DS(Constant.DATA_SOURCE_MYSQL)
public interface ClazzMapper extends SuperMapper<Clazz> {

    List<Clazz> findList(Page<Clazz> page, @Param("p") Map<String, Object> params, @Param("key") String key);

    /**
     * 获取班主任管理的班级信息
     *
     * @param schoolId 学校id
     * @param userId   班主任用户id
     * @return
     */
    List<ClazzDTO> getListByHeaderTeacher(@Param("schoolId") Long schoolId, @Param("userId") Long userId);

    /**
     * 获取班级id列表
     *
     * @param schoolId
     * @return
     */
    Set<Long> getClazzIdBySchool(@Param("schoolId") Long schoolId);

    /**
     * 判断班主任管理的班级中是否有该学生
     *
     * @param headerTeacherUserId 班主任用户id
     * @param studentUserId       学生用户id
     * @return 总条数
     */
    Boolean checkHeaderTeacherWithStudent(@Param("headerTeacherUserId") Long headerTeacherUserId, @Param("studentUserId") Long studentUserId);

    /**
     * 根据学校id和年级code获取所有班级列表
     *
     * @param schoolId 学校id
     * @param grade    年级编码
     * @return 班级列表
     */
    List<Long> getClazzBySchoolIdAndGrade(@Param("schoolId") Long schoolId, @Param("grade") String grade);
}
