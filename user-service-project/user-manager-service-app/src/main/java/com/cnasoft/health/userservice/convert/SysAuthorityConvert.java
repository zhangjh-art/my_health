package com.cnasoft.health.userservice.convert;

import com.cnasoft.health.common.dto.SysAuthorityDTO;
import com.cnasoft.health.common.dto.SysAuthoritySimpleDTO;
import com.cnasoft.health.common.util.CustomConverter;
import com.cnasoft.health.userservice.feign.dto.SysAuthorityReqVO;
import com.cnasoft.health.userservice.model.SysAuthority;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author cnasoft
 * @date 2020/8/16 10:39
 */
@Mapper(uses = CustomConverter.class)
public interface SysAuthorityConvert {
    SysAuthorityConvert INSTANCE = Mappers.getMapper(SysAuthorityConvert.class);

    /**
     * 权限请求数据转换为实体类
     *
     * @param reqVO 请求数据
     * @return 权限对象
     */
    @Mapping(target = "interfaces", source = "interfaces", qualifiedByName = "toJsonString")
    SysAuthority convertVO(SysAuthorityReqVO reqVO);

    /**
     * 权限实体转换为数据
     *
     * @param authority 权限对象
     * @return 数据对象
     */
    SysAuthorityDTO convert(SysAuthority authority);

    /**
     * 权限实体集合转换为数据集合
     *
     * @param sysAuthorities 权限对象列表
     * @return 数据对象列表
     */
    List<SysAuthorityDTO> convertList(List<SysAuthority> sysAuthorities);

    /**
     * 权限实体集合转换为数据集合
     *
     * @param sysAuthorities 权限对象列表
     * @return 数据对象列表
     */
    List<SysAuthoritySimpleDTO> convertSimpleList(List<SysAuthority> sysAuthorities);
}
