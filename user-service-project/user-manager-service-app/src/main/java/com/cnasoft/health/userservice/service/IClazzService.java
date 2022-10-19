package com.cnasoft.health.userservice.service;

import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.ClazzDTO;
import com.cnasoft.health.common.service.ISuperService;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.feign.dto.ClazzReqDTO;
import com.cnasoft.health.userservice.model.Clazz;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lqz
 * 针对表【class】的数据库操作Service
 * 2022-04-13
 */
public interface IClazzService extends ISuperService<Clazz> {

    /**
     * 获取班级列表
     *
     * @param params 查询条件
     * @return
     */
    PageResult<ClazzDTO> findList(Map<String, Object> params);

    /**
     * 获取所有班级列表
     *
     * @return
     */
    List<ClazzDTO> listAll(Long schoolId);

    /**
     * 通过年级查询班级列表
     *
     * @param grade 年级编码
     * @return list
     */
    List<ClazzDTO> listClazzByGrade(String grade);

    /**
     * 通过班级名称查询班级列表
     *
     * @param clazzName 班级名
     * @return list
     */
    List<ClazzDTO> listClazzByClazzName(String clazzName);

    /**
     * 通过年级和班级名称查询班级详情
     *
     * @param grade     年级编码
     * @param clazzName 班级名
     * @return ClazzDTO
     */
    ClazzDTO getClazzDetail(String grade, String clazzName);

    /**
     * 新增班级
     *
     * @param clazzDTO 班级信息
     *                 自定义异常
     */
    void saveClazz(ClazzReqDTO clazzDTO);

    /**
     * 、
     * 更新班级信息
     *
     * @param clazzDTO 班级信息
     *                 自定义异常
     */
    void updateClazz(ClazzReqDTO clazzDTO);

    /**
     * 逻辑删除/批量删除班级信息
     *
     * @param ids 班级id集合
     * @return int
     */
    List<BatchOperationTipDTO> deleteClazz(@Param("ids") Set<Long> ids);

    /**
     * 获取班主任管理的班级信息
     *
     * @return
     */
    List<ClazzDTO> getListByHeaderTeacher();

    /**
     * 根据指定班级id列表获取班级信息
     *
     * @param ids 班级id列表
     * @return
     */
    List<ClazzDTO> getListByIds(Set<Long> ids);

    /**
     * 根据学校id删除
     *
     * @param schoolId 学校id
     */
    void deleteBySchoolId(Long schoolId);

    /**
     * 判断班主任管理的班级中是否有该学生
     *
     * @param headerTeacherUserId 班主任用户id
     * @param studentUserId       学生用户id
     * @return
     */
    Boolean checkHeaderTeacherWithStudent(Long headerTeacherUserId, Long studentUserId);

    /**
     * 根据学校id和年级code获取所有班级列表
     *
     * @param schoolId 学校id
     * @param grade    年级编码
     * @return 班级列表
     */
    List<Long> getClazzBySchoolIdAndGrade(Long schoolId, String grade);
}
