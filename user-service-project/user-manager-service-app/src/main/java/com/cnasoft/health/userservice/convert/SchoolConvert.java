package com.cnasoft.health.userservice.convert;

import com.cnasoft.health.common.dto.CommonDTO;
import com.cnasoft.health.common.dto.SchoolDTO;
import com.cnasoft.health.userservice.model.School;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author zcb
 * 2022/4/6
 */
@Mapper
public interface SchoolConvert {
    SchoolConvert INSTANCE = Mappers.getMapper(SchoolConvert.class);

    /**
     * 实体集合转换为数据传输对象
     *
     * @param list 学校model数据列表
     * @return list
     */
    List<SchoolDTO> convertList(List<School> list);

    /**
     * 学校module数据转换为数据传输对象
     *
     * @param school 学校model数据
     * @return SchoolDTO
     */
    SchoolDTO convert(School school);

    /**
     * 学校module数据转换为数据传输对象
     *
     * @param schoolDTO 学校传输数据
     * @return School
     */
    School convertData(SchoolDTO schoolDTO);

    /**
     * 将学校ID和name返回
     *
     * @param schools
     * @return
     */
    List<CommonDTO> convertCommon(List<School> schools);
}
