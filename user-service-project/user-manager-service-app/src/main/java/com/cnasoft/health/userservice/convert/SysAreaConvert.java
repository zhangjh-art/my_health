package com.cnasoft.health.userservice.convert;

import com.cnasoft.health.common.dto.SysAreaDTO;
import com.cnasoft.health.userservice.feign.dto.SysAreaReqVO;
import com.cnasoft.health.userservice.model.SysArea;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author ganghe
 * @date 2022/4/9 17:24
 **/
@Mapper
public interface SysAreaConvert {

    SysAreaConvert INSTANCE = Mappers.getMapper(SysAreaConvert.class);

    /**
     * DTO转换为实体对象
     *
     * @param createReqVO
     * @return
     */
    SysArea convertVO(SysAreaReqVO createReqVO);

    /**
     * 实体对象转换为DTO
     *
     * @param area
     * @return
     */
    SysAreaDTO convert(SysArea area);
}
