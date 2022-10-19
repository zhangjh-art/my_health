package com.cnasoft.health.userservice.convert;

import com.cnasoft.health.common.dto.SchoolDTO;
import com.cnasoft.health.common.util.CustomConverter;
import com.cnasoft.health.userservice.feign.dto.SchoolTeacherReqVO;
import com.cnasoft.health.userservice.feign.dto.SchoolTeacherRespVO;
import com.cnasoft.health.userservice.model.SchoolTeacher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Classname SchoolTeacherConvert
 * @Description 实体类转化
 * @Date 2022/4/14 11:21
 * @Created by Shadow
 */
@Mapper(uses = CustomConverter.class)
public interface SchoolTeacherConvert {
    SchoolTeacherConvert INSTANCE = Mappers.getMapper(SchoolTeacherConvert.class);


    /**
     * 实体集合转换为数据传输对象
     *
     * @param list
     * @return
     */
    List<SchoolDTO> convertList(List<SchoolTeacher> list);

    /**
     * 实体集合转换为数据传输对象
     *
     * @param list
     * @return
     */

    List<SchoolTeacherRespVO> convert2List(List<SchoolTeacher> list);

    /**
     * 实体类转换为数据对象
     *
     * @param teacher
     * @return
     */
    @Mapping(target = "certificationFile", source = "certificationFile", qualifiedByName = "toStrList")
    SchoolTeacherRespVO convert(SchoolTeacher teacher);

    /**
     * 数据传输对象转实体对象
     *
     * @param teacherReqVO
     * @return
     */
    @Mapping(target = "certificationFile", source = "certificationFile", qualifiedByName = "toJsonString")
    SchoolTeacher convert(SchoolTeacherReqVO teacherReqVO);
}
