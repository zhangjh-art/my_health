package com.cnasoft.health.userservice.convert;

import com.cnasoft.health.common.dto.SysRoleDTO;
import com.cnasoft.health.userservice.feign.dto.SysRoleCreateReqVO;
import com.cnasoft.health.userservice.feign.dto.SysRoleUpdateReqVO;
import com.cnasoft.health.userservice.model.SysRole;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;

/**
 * @author cnasoft
 * @date 2020/8/16 10:52
 */
@Mapper
public interface SysRoleConvert {
    SysRoleConvert INSTANCE = Mappers.getMapper(SysRoleConvert.class);

    /**
     * 将实例集合转换为DTO集合
     *
     * @param sysRoleList 数据集合
     * @return 集合
     */
    List<SysRoleDTO> convertList(List<SysRole> sysRoleList);

    /**
     * 将实例集合转换为DTO集合
     *
     * @param sysRoleList
     * @return
     */
    Set<SysRoleDTO> convertList(Set<SysRole> sysRoleList);

    /**
     * 将DTO对象转换为实体类
     *
     * @param createReqVO DTO
     * @return 实体对象
     */
    SysRole convertVO(SysRoleCreateReqVO createReqVO);

    /**
     * 将DTO对象转换为实体类
     *
     * @param updateReqVO DTO
     * @return 实体对象
     */
    SysRole convertVO(SysRoleUpdateReqVO updateReqVO);

    /**
     * 将实体类转换为DTO对象
     *
     * @param role 实体对象
     * @return DTO
     */
    SysRoleDTO convert(SysRole role);
}
