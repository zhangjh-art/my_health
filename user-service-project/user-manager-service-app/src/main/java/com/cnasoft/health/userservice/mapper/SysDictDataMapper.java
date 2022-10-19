package com.cnasoft.health.userservice.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.dto.SysDictDTO;
import com.cnasoft.health.db.autoconfigure.mapper.SuperMapper;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.feign.dto.DictNameReqVO;
import com.cnasoft.health.userservice.model.SysDictData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author lqz
 */
@DS(Constant.DATA_SOURCE_MYSQL)
@Mapper
public interface SysDictDataMapper extends SuperMapper<SysDictData> {

    /**
     * 分页查询字段列表
     *
     * @param page
     * @param query
     * @return
     */
    List<SysDictData> findList(Page<SysDictDTO> page, @Param("query") DictNameReqVO query);

    /**
     * 查询有审核记录的字典id
     *
     * @param dictName 名称
     * @param dictType 类型
     * @return
     */
    List<Long> getDictDateIdByQuery(@Param("dictName") String dictName, @Param("dictType") String dictType);

    /**
     * 更新字典数据审核状态,其他模块调用
     *
     * @param dictDataIds   id列表
     * @param approveStatus 审核状态
     * @return
     */
    int updateApproveStatus(@Param("dictDataIds") List<Long> dictDataIds, @Param("approveStatus") Integer approveStatus);

    /**
     * 查询字典数据id列表
     *
     * @param dictValues 字典数据集合
     * @return
     */
    List<Long> findDictDataByDictValues(@Param("dictValues") Set<String> dictValues);

    /**
     * 根据字典类型查询字典数据(独占锁)
     *
     * @param dictType 字典类型
     * @return
     */
    List<SysDictData> findDictDataByType(@Param("dictType") String dictType);
}
