package com.cnasoft.health.userservice.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.model.SysRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author cnasoft
 * @date 2020/8/13 10:37
 */
@DS(Constant.DATA_SOURCE_MYSQL)
public interface SysRoleMapper extends SuperMapper<SysRole> {
    /**
     * 分页查询角色
     *
     * @param page   分页对象
     * @param params 查询条件
     * @return 角色列表
     */
    List<SysRole> findList(Page<SysRole> page, @Param("param") Map<String, Object> params);


    /**
     * 查询有审核记录的角色id
     *
     * @param keyword 角色名称
     * @return
     */
    List<Long> getApproveAreaId(@Param("keyword") String keyword);

    /**
     * 根据id查询角色信息(逻辑删除的数据也能查询)
     *
     * @param id
     * @return
     */
    SysRole findById(Long id);
}
