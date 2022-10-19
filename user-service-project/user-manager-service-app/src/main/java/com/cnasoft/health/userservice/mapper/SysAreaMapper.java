package com.cnasoft.health.userservice.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.dto.SysAreaDTO;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.model.SysArea;
import org.apache.ibatis.annotations.Param;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lgf
 * @date 2022/3/23.
 */
public interface SysAreaMapper extends SuperMapper<SysArea> {
    /**
     * 查询区域列表
     *
     * @param params
     * @return
     */
    List<SysAreaDTO> getAreaList(@Param("p") Map<String, Object> params);

    /**
     * 查询有审核记录的区域id
     *
     * @param keyword 省名称、市名称、区名称
     * @return
     */
    List<Long> getApproveAreaId(@Param("keyword") String keyword);

    /**
     * 查询区域列表
     *
     * @param page   分页对象
     * @param params 查询条件
     * @return
     */
    List<SysAreaDTO> getAreaList(Page<SysAreaDTO> page, @Param("p") Map<String, Object> params);

    /**
     * 获取区域数据最新更新时间
     *
     * @return Date
     */
    Date selectLastUpdateTime();

    /**
     * 查询数据,已删除的也可以查询
     *
     * @param code
     * @return
     */
    SysArea selectByCode(Integer code);

    @Override
    int deleteById(Serializable id);

    /**
     * 根据省编码查询所有区域
     *
     * @param provinceCode 省编码
     * @return
     */
    Set<Long> getAreaIdByProvince(@Param("code") String provinceCode);

    /**
     * 根据市编码查询所有区域
     *
     * @param cityCode 市编码
     * @return
     */
    Set<Long> getAreaIdByCity(@Param("code") String cityCode);
}
