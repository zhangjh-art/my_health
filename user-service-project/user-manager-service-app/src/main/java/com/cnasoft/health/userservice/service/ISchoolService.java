package com.cnasoft.health.userservice.service;

import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.CommonDTO;
import com.cnasoft.health.common.dto.SchoolDTO;
import com.cnasoft.health.common.service.ISuperService;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.feign.dto.SchoolReqVO;
import com.cnasoft.health.userservice.model.School;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lqz
 * 针对表【school(学校表)】的数据库操作Service
 * 2022-04-11
 */
public interface ISchoolService extends ISuperService<School> {

    /**
     * 分页查询学校列表
     *
     * @param params
     * @return
     */
    PageResult<SchoolDTO> listSchool(Map<String, Object> params);

    /**
     * 根据学校id列表返回学校名称
     *
     * @param schoolIds
     * @return
     */
    List<CommonDTO> listSchool(Set<Long> schoolIds);

    /**
     * 查询所有学校
     *
     * @param areaCodes
     * @return
     */
    List<SchoolDTO> listAllSchool(List<Integer> areaCodes);

    /**
     * 根据学校id列表获取学校信息列表
     *
     * @param ids id列表
     * @return
     */
    List<SchoolDTO> getSchoolListByIds(Set<Long> ids);

    /**
     * 获取学校详情
     *
     * @param schoolId 学校id
     * @return 学校详情
     */
    SchoolDTO getSchoolDetail(Long schoolId);

    /**
     * 添加学校
     *
     * @param reqVO 学校请求数据
     * @return 学校信息
     */
    School saveSchool(SchoolReqVO reqVO);

    /**
     * 编辑学校
     *
     * @param reqVO 学校请求数据
     */
    void updateSchool(SchoolReqVO reqVO);

    /**
     * 删除/批量删除学校
     *
     * @param ids 学校id集合
     * @return int
     */
    List<BatchOperationTipDTO> deleteSchool(Set<Long> ids);

    /**
     * 批量删除学校
     *
     * @param areaCode 区域编码
     */
    void deleteByAreaCode(Integer areaCode);

    /**
     * 学校增量统计(当年)
     */
    List<Map<String, Object>> getSchoolStatistics();
}
