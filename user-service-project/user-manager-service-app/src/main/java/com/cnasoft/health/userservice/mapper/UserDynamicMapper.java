package com.cnasoft.health.userservice.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.feign.dto.DynamicWarningRespVO;
import com.cnasoft.health.userservice.feign.dto.UserDynamicDTO;
import com.cnasoft.health.userservice.model.UserDynamic;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */
public interface UserDynamicMapper extends SuperMapper<UserDynamic> {

    List<UserDynamicDTO> selectUserDynamicList(Page<UserDynamic> page, @Param("params") Map<String, Object> params);

    void updateSort(@Param("dynamicId") Long dynamicId, @Param("sort") int sort);

    void updateResetSort(@Param("userId") Long userId);

    void deleteDynamic(@Param("dynamicId") Long dynamicId);

    /**
     * 校心理老师查询学生动态预警
     *
     * @param page   分页参数
     * @param params 查询参数
     * @param key    解密key
     * @return List<DynamicWarningRespVO>
     */
    List<DynamicWarningRespVO> getStudentDynamicWarning(Page<DynamicWarningRespVO> page,
                                                        @Param("param") Map<String, Object> params,
                                                        @Param("key") String key);

    /**
     * 校心理老师查询学生动态预警
     *
     * @param page   分页参数
     * @param params 查询参数
     * @param key    解密key
     * @return List<DynamicWarningRespVO>
     */
    List<DynamicWarningRespVO> getParentDynamicWarning(Page<DynamicWarningRespVO> page,
                                                       @Param("param") Map<String, Object> params,
                                                       @Param("key") String key);

    /**
     * 校心理老师查询教职工、老师动态预警
     *
     * @param page   分页参数
     * @param params 查询参数
     * @param key    解密key
     * @return List<DynamicWarningRespVO>
     */
    List<DynamicWarningRespVO> getTeacherDynamicWarning(Page<DynamicWarningRespVO> page,
                                                        @Param("param") Map<String, Object> params,
                                                        @Param("key") String key);

    /**
     * 根据id查询本校的动态预警
     *
     * @param id       预警id
     * @param schoolId 学校id
     * @param areaCode 区域code
     * @return UserDynamic
     */
    UserDynamic selectDynamicById(@Param("id") Long id,
                                  @Param("schoolId") Long schoolId,
                                  @Param("areaCode") Integer areaCode);

    /**
     * 区域心理教研员查询动态预警列表
     *
     * @param page   分页
     * @param params 查询参数
     * @param key    解密key
     * @return List<DynamicWarningRespVO>  列表数据
     */
    List<DynamicWarningRespVO> getAreaDynamicWarning(Page<DynamicWarningRespVO> page,
                                                     @Param("param") Map<String, Object> params,
                                                     @Param("key") String key);

    /**
     * 查询动态预警详情，包含已删除的
     * @param id
     * @return
     */
    UserDynamic selectUserDynamic(@Param("id") Long id);

    /**
     * 修改数据状态为正常业务可用状态
     *
     * @param id
     */
    void fullData(@Param("id") Long id);
}
