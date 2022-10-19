package com.cnasoft.health.userservice.mapper;

import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.model.ReservationTimeConfig;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ReservationTimeConfigMapper extends SuperMapper<ReservationTimeConfig> {

    /**
     * 根据config id删除时间段配置
     *
     * @param configId
     * @return
     */
    int deleteByConfigId(@Param("configId") Long configId);

    /**
     * 根据日期获取控闲时间段
     *
     * @return
     */
    List<Map<String, Object>> getAvailableTimeByDate(@Param("schoolId") Long schoolId,
        @Param("areaCode") Integer areaCode, @Param("weekDay") Integer weekDay, @Param("date") Date date);
}
