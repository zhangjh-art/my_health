package com.cnasoft.health.userservice.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.dto.SchoolDTO;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.model.School;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zcb
 * 针对表【school(学校表)】的数据库操作Mapper
 * 2022-04-06
 */
public interface SchoolMapper extends SuperMapper<School> {
    /**
     * 分页查询学校信息
     *
     * @param page
     * @param params
     * @return
     */
    List<School> findList(Page<SchoolDTO> page, @Param("param") Map<String, Object> params);


    /**
     * 查询有审核记录的学校id
     *
     * @param keyword 省名称、市名称、区名称
     * @return
     */
    List<Long> getApproveAreaId(@Param("keyword") String keyword);

    /**
     * 根据区域编码获取学校id
     *
     * @param areaCode
     * @return
     */
    Set<Long> getIdByAreaCode(@Param("areaCode") Integer areaCode);

    /**
     * 学校增量统计(当年)
     *
     * @param date 统计开始时间
     * @return
     */
    List<Map<String, Object>> getSchoolStatistics(@Param("date") String date);
}
