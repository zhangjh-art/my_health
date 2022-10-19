package com.cnasoft.health.userservice.service;

import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.SysAreaDTO;
import com.cnasoft.health.common.service.ISuperService;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.model.SysArea;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 区域管理
 *
 * @author ganghe
 */
public interface ISysAreaService extends ISuperService<SysArea> {
    /**
     * 通过自定义数据查询区域列表
     *
     * @param params 参数列表
     * @return 数据集
     */
    Set<SysAreaDTO> getAreaList(Map<String, Object> params);

    /**
     * 缓存所有区域数据
     */
    void cacheAllArea();

    /**
     * 新增区域数据
     *
     * @param area 区域数据
     * @return 区域数据
     */
    SysArea saveArea(SysArea area);

    /**
     * 更新区域数据
     *
     * @param area 区域数据
     */
    void updateArea(SysArea area);

    /**
     * 更新数据之前校验数据合法性
     *
     * @param area 区域数据
     * @return 区域数据
     */
    SysArea beforeUpdateValid(SysArea area);

    /**
     * 更新区域状态
     *
     * @param id      区域id
     * @param enabled 启用/禁用
     */
    void updateEnabled(Long id, Boolean enabled);

    /**
     * 删除区域数据
     *
     * @param ids id列表
     * @return 通用结果
     */
    List<BatchOperationTipDTO> delete(Set<Long> ids);

    /**
     * 获取区域数据上次更新时间
     *
     * @return 最后一次更新时间
     */
    Date getLastUpdateTime();

    /**
     * 分页查询区域数据
     *
     * @param params 查询条件
     * @return 区域数据
     */
    PageResult<SysAreaDTO> getAreaListPage(Map<String, Object> params);

    /**
     * 获取区域省市区名称
     *
     * @param areaDTO 区域基础信息
     */
    void getAreaNameInfo(SysAreaDTO areaDTO);

    /**
     * 获取区域可用状态
     *
     * @param areaCode 区域编码
     * @return 可用状态
     */
    Boolean getAreaAvailableStatus(Integer areaCode);

    /**
     * 获取区域信息
     *
     * @param areaCode 区域编码
     * @return 区域数据
     */
    SysAreaDTO getArea(Integer areaCode);
}
