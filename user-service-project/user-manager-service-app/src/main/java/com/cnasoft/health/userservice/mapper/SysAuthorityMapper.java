package com.cnasoft.health.userservice.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.dto.SysAuthorityDTO;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.model.School;
import com.cnasoft.health.userservice.model.SysAuthority;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author ganghe
 */
@DS(Constant.DATA_SOURCE_MYSQL)
@Mapper
public interface SysAuthorityMapper extends SuperMapper<SysAuthority> {

    /**
     * 根据角色ID查询权限编码列表
     *
     * @param authorityIds 权限ID
     * @return 权限编码列表
     */
    Set<String> findAuthoritiesByAuthId(@Param("authorityIds") Set<Long> authorityIds);

    /**
     * 分页查询权限数据
     *
     * @param page   分页参数
     * @param params 查询条件
     * @return
     */
    List<SysAuthorityDTO> findList(Page<SysAuthorityDTO> page, @Param("param") Map<String, Object> params);
}
