package com.cnasoft.health.userservice.mapper;

import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.model.ConsultationReport;
import org.apache.ibatis.annotations.Param;

public interface ConsultationReportMapper extends SuperMapper<ConsultationReport> {
    /**
     * @param id
     */
    void fullData(@Param("id") String id);
}
