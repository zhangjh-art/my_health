package com.cnasoft.health.userservice.convert;

import com.cnasoft.health.common.dto.ClazzDTO;
import com.cnasoft.health.userservice.feign.dto.ClazzReqDTO;
import com.cnasoft.health.userservice.model.Clazz;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author lqz
 * 2022/4/12
 */
@Mapper
public interface ClazzConvert {
    ClazzConvert INSTANCE = Mappers.getMapper(ClazzConvert.class);


    /**
     * 实体集合转换为数据传输对象
     *
     * @param list  班级数据列表
     * @return 班级传输对象列表
     */
    List<ClazzDTO> convertList(List<Clazz> list);

    ClazzDTO convertDTO(Clazz clazz);

    Clazz convertDb(ClazzReqDTO clazzDTO);
}
