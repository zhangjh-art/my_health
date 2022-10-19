package com.cnasoft.health.userservice.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.model.SysAuthority;
import com.cnasoft.health.userservice.model.SysUserAuthority;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author ganghe
 */
@DS(Constant.DATA_SOURCE_MYSQL)
@Mapper
public interface SysUserAuthorityMapper extends SuperMapper<SysUserAuthority> {

    /**
     * 根据用户id获取权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<SysAuthority> findAuthoritiesByUserId(@Param("userId") Long userId);

    /**
     * 查询所有用户的权限数据
     *
     * @param userIds 用户ID列表
     * @return 用户权限列表
     */
    List<SysAuthority> findAuthorities(@Param("userIds") List<Long> userIds);
}

