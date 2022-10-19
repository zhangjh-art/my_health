package com.cnasoft.health.userservice.convert;

import com.cnasoft.health.common.util.CustomConverter;
import com.cnasoft.health.userservice.feign.dto.AreaTeacherReqVO;
import com.cnasoft.health.userservice.feign.dto.AreaTeacherRespVO;
import com.cnasoft.health.userservice.model.AreaTeacher;
import com.cnasoft.health.userservice.model.SysUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = CustomConverter.class)
public interface AreaTeacherConvert {
    AreaTeacherConvert INSTANCE = Mappers.getMapper(AreaTeacherConvert.class);

    /**
     * 实体集合转换为数据传输对象
     *
     * @param list 区域心理教研员数据列表
     * @return 区域心理教研员传输对象列表
     */
    List<AreaTeacherRespVO> convertList(List<AreaTeacher> list);

    /**
     * 实体集合转换为数据传输对象
     *
     * @param areaTeacher 区域心理教研员数据
     * @return AreaTeacherRespVO 区域心理教研员传输对象
     */
    @Mapping(target = "certificationFile", source = "certificationFile", qualifiedByName = "toStrList")
    AreaTeacherRespVO convertVO(AreaTeacher areaTeacher);

    /**
     * 数据传输对象转换为实体集合
     *
     * @param teacherReqVO 区域心理教研员新增数据
     * @return AreaTeacher 区域心理教研员数据库存储数据
     */
    @Mapping(target = "certificationFile", source = "certificationFile", qualifiedByName = "toJsonString")
    AreaTeacher convertDb(AreaTeacherReqVO teacherReqVO);

    /**
     * 数据传输对象转换为实体集合
     *
     * @param teacherReqVO 区域心理教研员新增数据
     * @return SysUser 区域心理教研员用户数据
     */
    SysUser convertSysUser(AreaTeacherReqVO teacherReqVO);
}
