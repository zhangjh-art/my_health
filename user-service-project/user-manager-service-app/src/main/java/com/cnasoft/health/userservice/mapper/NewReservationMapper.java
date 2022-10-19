package com.cnasoft.health.userservice.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.model.NewReservation;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface NewReservationMapper extends SuperMapper<NewReservation> {

    /**
     * 根据年月获取每日预约统计数据
     *
     * @param year  年份
     * @param month 月份
     * @return 指定月份每日预约数据
     */
    List<Map<String, Object>> getStatisticalData(@Param("year") Integer year, @Param("month") Integer month,
        @Param("userId") Long userId);

    /**
     * 条件查询预约数据
     *
     * @return 预约列表
     */
    List<NewReservation> list(Page<NewReservation> page, @Param("p") Map<String, Object> params,
        @Param("key") String key);

    /**
     * 修改数据状态为正常业务可用状态
     *
     * @param id
     */
    void fullData(@Param("id") String id);
}
