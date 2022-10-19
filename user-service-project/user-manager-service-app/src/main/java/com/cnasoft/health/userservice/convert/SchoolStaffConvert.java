package com.cnasoft.health.userservice.convert;

import com.cnasoft.health.common.dto.SchoolDTO;
import com.cnasoft.health.common.util.CustomConverter;
import com.cnasoft.health.userservice.feign.dto.SchoolStaffReqVO;
import com.cnasoft.health.userservice.feign.dto.SchoolStaffRespVO;
import com.cnasoft.health.userservice.model.SchoolStaff;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Administrator
 * @Classname SchoolStaffConvert
 * @Description 实体类转化
 * @Date 2022/4/14 11:21
 * @Created by Shadow
 */
@Mapper(uses = CustomConverter.class)
public interface SchoolStaffConvert {
    SchoolStaffConvert INSTANCE = Mappers.getMapper(SchoolStaffConvert.class);


    /**
     * 实体集合转换为数据传输对象
     *
     * @param list
     * @return
     */
    List<SchoolDTO> convertList(List<SchoolStaff> list);

    /**
     * 实体集合转换为数据传输对象
     *
     * @param list
     * @return
     */

    List<SchoolStaffRespVO> convert2List(List<SchoolStaff> list);

    /**
     * 实体类转换为数据对象
     *
     * @param teacher
     * @return
     */
    SchoolStaffRespVO convert(SchoolStaff teacher);

    /**
     * 数据传输对象转实体对象
     *
     * @param staffReqVO
     * @return
     */
    SchoolStaff convert(SchoolStaffReqVO staffReqVO);
}
