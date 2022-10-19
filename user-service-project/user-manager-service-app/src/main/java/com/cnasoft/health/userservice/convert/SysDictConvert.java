package com.cnasoft.health.userservice.convert;

import com.cnasoft.health.common.dto.SysDictDTO;
import com.cnasoft.health.userservice.model.SysDictData;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Administrator
 */
@Mapper
public interface SysDictConvert {
    SysDictConvert INSTANCE = Mappers.getMapper(SysDictConvert.class);

    /**
     * DTO转换为实体对象
     *
     * @param sysDictDTO
     * @return
     */
    SysDictData convertDictData(SysDictDTO sysDictDTO);

    /**
     * 实体对象转换为DTO
     *
     * @param sysDictData
     * @return
     */
    SysDictDTO convertDictDTO(SysDictData sysDictData);

    /**
     * 实体对象集合转换为DTO集合
     *
     * @param list
     * @return
     */
    List<SysDictDTO> convertDTOList(List<SysDictData> list);
}
