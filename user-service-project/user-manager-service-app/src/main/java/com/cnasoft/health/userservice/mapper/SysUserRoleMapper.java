package com.cnasoft.health.userservice.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.model.SysRole;
import com.cnasoft.health.userservice.model.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author ganghe
 */
@DS(Constant.DATA_SOURCE_MYSQL)
@Mapper
public interface SysUserRoleMapper extends SuperMapper<SysUserRole> {

    /**
     * 根据用户id获取角色
     *
     * @param userId 角色ID
     * @return 用户集合
     */
    List<SysRole> findRolesByUserId(@Param("userId") Long userId);

}