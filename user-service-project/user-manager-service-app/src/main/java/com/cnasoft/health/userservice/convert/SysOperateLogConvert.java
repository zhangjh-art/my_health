package com.cnasoft.health.userservice.convert;

import com.cnasoft.health.common.dto.SysOperateLogDTO;
import com.cnasoft.health.userservice.model.SysOperateLog;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author ganghe
 */
@Mapper
public interface SysOperateLogConvert {
    SysOperateLogConvert INSTANCE = Mappers.getMapper(SysOperateLogConvert.class);

    /**
     * DTO转换为实体对象
     *
     * @param operateLogDTO
     * @return
     */
    SysOperateLog convert(SysOperateLogDTO operateLogDTO);
}
