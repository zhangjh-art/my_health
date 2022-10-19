package com.cnasoft.health.userservice.service;

import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.SysDictDTO;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.service.ISuperService;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.feign.dto.DictNameReqVO;
import com.cnasoft.health.userservice.model.SysDictData;
import com.cnasoft.health.userservice.model.SysDictType;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lqz
 */
public interface ISysDictService extends ISuperService<SysDictData> {

    /**
     * 查询所有的字典类型数据
     *
     * @return 数据列表
     */
    List<SysDictType> listDictType();

    /**
     * 根据字典类型查询字段数据
     *
     * @param dictType      字典类型
     * @param disable       启用禁用
     * @param approveStatus 审核状态
     * @return 数据列表
     */
    List<SysDictData> listDictData(String dictType, Boolean disable, ApproveStatus approveStatus);

    /**
     * 通过类型查询数据字典列表
     *
     * @param dictType      类型  部门，年级
     * @param useCache      是否使用缓存数据
     * @param approveStatus 审核状态
     * @return 数据集
     */
    List<SysDictDTO> listDictDataByType(String dictType, Boolean useCache, ApproveStatus approveStatus);

    /**
     * 通过多种类型查询数据字典列表
     *
     * @param dictTypes     类型  部门，年级
     * @param useCache      是否使用缓存数据
     * @param approveStatus 审核状态
     * @return 数据集
     */
    Map<String, List<SysDictDTO>> listDictDataByTypes(List<String> dictTypes, Boolean useCache, ApproveStatus approveStatus);

    /**
     * 插入字典数据
     *
     * @param dictDTO 请求数据
     * @return 字典值
     */
    SysDictData saveDictData(SysDictDTO dictDTO);

    /**
     * 插入字典数据-其他模块调用
     *
     * @param dictDTO 请求数据
     * @return 字典值
     */
    SysDictData saveDictDataOtherModule(SysDictDTO dictDTO);

    /**
     * 修改字典数据
     *
     * @param dictDTO 请求数据
     */
    void updateDictData(SysDictDTO dictDTO);

    /**
     * 修改字典数据审核状态,其他模块调用
     *
     * @param gaugeTypeSet  量表类型集合
     * @param approveStatus 审核状态
     */
    void updateDictDataApproveStatus(Set<String> gaugeTypeSet, Integer approveStatus);

    SysDictData beforeUpdateValid(SysDictDTO dictDTO);

    void cacheDictData(Long id);

    /**
     * 批量删除字典数据
     *
     * @param ids id列表
     * @return 通用提示信息
     */
    List<BatchOperationTipDTO> deleteDictData(Set<Long> ids);

    /**
     * 分页查询数据词典
     *
     * @param query 查询条件
     * @return 分页数据
     */
    PageResult<SysDictDTO> listPage(DictNameReqVO query);

    /**
     * 获取H5端，轮播图列表
     *
     * @return 轮播图访问地址列表
     */
    List<String> getH5BannerList();
}
