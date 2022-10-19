package com.cnasoft.health.userservice.service;

import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.SysRoleDTO;
import com.cnasoft.health.common.service.ISuperService;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.feign.dto.SysRoleCreateReqVO;
import com.cnasoft.health.userservice.feign.dto.SysRoleUpdateReqVO;
import com.cnasoft.health.userservice.model.SysRole;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author cnasoft
 * @date 2020/8/13 10:57
 */
public interface ISysRoleService extends ISuperService<SysRole> {

    /**
     * 角色列表
     *
     * @param params 查询条件
     * @return 分页数据
     */
    PageResult<SysRoleDTO> findRoles(Map<String, Object> params);

    /**
     * 查询所有角色
     *
     * @return 角色列表
     */
    List<SysRoleDTO> findAll();

    /**
     * 新增角色
     *
     * @param createReqVO 请求数据
     * @return 角色数据
     * @throws Exception 异常
     */
    SysRole saveRole(SysRoleCreateReqVO createReqVO) throws Exception;

    /**
     * 修改角色
     *
     * @param updateReqVO 请求数据
     */
    void updateRole(SysRoleUpdateReqVO updateReqVO);

    /**
     * 删除角色
     *
     * @param ids id列表
     * @return 统一提示结果
     */
    List<BatchOperationTipDTO> deleteRole(Set<Long> ids);
}
