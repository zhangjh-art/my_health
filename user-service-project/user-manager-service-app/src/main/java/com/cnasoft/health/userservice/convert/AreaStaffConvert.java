package com.cnasoft.health.userservice.convert;

import com.cnasoft.health.userservice.feign.dto.AreaStaffReqVO;
import com.cnasoft.health.userservice.feign.dto.AreaStaffRespVO;
import com.cnasoft.health.userservice.model.AreaStaff;
import com.cnasoft.health.userservice.model.SysUser;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface AreaStaffConvert {
    AreaStaffConvert INSTANCE = Mappers.getMapper(AreaStaffConvert.class);

    /**
     * 实体集合转换为数据传输对象
     *
     * @param areaStaff 区域职员数据
     * @return AreaStaffVO 区域职员传输对象
     */
    AreaStaffRespVO convertVO(AreaStaff areaStaff);

    /**
     * 数据传输对象转换为实体集合
     *
     * @param areaStaffReqVO 区域职员传输对象
     * @return AreaStaff 区域职员数据库数据
     */
    AreaStaff convertDb(AreaStaffReqVO areaStaffReqVO);

    /**
     * 数据传输对象转换为实体集合
     *
     * @param createReqVO 区域职员传输对象
     * @return AreaStaff 区域职员数据库数据
     */
    AreaStaff convert(AreaStaffReqVO createReqVO);

    /**
     * 数据传输对象转换为用户实体数据
     *
     * @param areaStaffReqVO 区域职员传输对象
     * @return SysUser 用户数据库数据
     */
    SysUser convertUser(AreaStaffReqVO areaStaffReqVO);

    /**
     * 实体数据列表转换为数据传输对象列表
     *
     * @param areaStaffs
     * @return
     */
    List<AreaStaffRespVO> convertList(List<AreaStaff> areaStaffs);
}
