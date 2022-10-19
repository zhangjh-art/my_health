package com.cnasoft.health.userservice.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.constant.CommonConstant;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.SysDictDTO;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.convert.SysDictConvert;
import com.cnasoft.health.userservice.feign.dto.DictNameReqVO;
import com.cnasoft.health.userservice.mapper.SysDictDataMapper;
import com.cnasoft.health.userservice.mapper.SysDictTypeMapper;
import com.cnasoft.health.userservice.model.SysDictData;
import com.cnasoft.health.userservice.model.SysDictType;
import com.cnasoft.health.userservice.service.IApproveService;
import com.cnasoft.health.userservice.service.ISysDictService;
import com.cnasoft.health.userservice.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.cnasoft.health.common.exception.constant.GlobalErrorCodeConstants.DATE_NOT_EXIST;

/**
 * @author lqz
 */
@Slf4j
@Service
public class SysDictServiceImpl extends SuperServiceImpl<SysDictDataMapper, SysDictData> implements ISysDictService {
    /**
     * 数据字典类型mapper
     */
    @Resource
    private SysDictTypeMapper sysDictTypeMapper;

    @Resource
    private SysDictDataMapper sysDictDataMapper;

    @Resource
    private IApproveService approveService;

    private static final int NINE = 9;

    @Override
    public List<SysDictType> listDictType() {
        return sysDictTypeMapper.selectList(null);
    }

    @Override
    public List<SysDictData> listDictData(String dictType, Boolean disable, ApproveStatus approveStatus) {
        LambdaQueryWrapper<SysDictData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDictData::getDictType, dictType);
        if (Objects.nonNull(approveStatus)) {
            queryWrapper.eq(SysDictData::getApproveStatus, approveStatus.getCode());
        }

        if (Objects.nonNull(disable)) {
            queryWrapper.eq(SysDictData::getDisable, disable);
        }

        return sysDictDataMapper.selectList(queryWrapper);
    }

    /**
     * 获取字典数据列表
     *
     * @param dictType 字典类型
     * @return SysDictDTO 字典数据列表
     */
    @Override
    public List<SysDictDTO> listDictDataByType(String dictType, Boolean useCache, ApproveStatus approveStatus) {
        if (Boolean.TRUE.equals(useCache)) {
            List<SysDictDTO> dictData = RedisUtils.getDictData(dictType);
            if (CollectionUtils.isEmpty(dictData)) {
                return SysDictConvert.INSTANCE.convertDTOList(listDictData(dictType, false, approveStatus));
            } else {
                return dictData;
            }
        } else {
            return SysDictConvert.INSTANCE.convertDTOList(listDictData(dictType, null, approveStatus));
        }
    }

    /**
     * 获取字典数据映射
     *
     * @param dictTypes 字典类型
     * @return SysDictDTO 字典数据列表
     */
    @Override
    public Map<String, List<SysDictDTO>> listDictDataByTypes(List<String> dictTypes, Boolean useCache, ApproveStatus approveStatus) {
        Map<String, List<SysDictDTO>> resultMap = new HashMap<>();
        for (String dataType : dictTypes) {
            resultMap.put(dataType, listDictDataByType(dataType, useCache, approveStatus));
        }
        return resultMap;
    }

    /**
     * 插入数据
     *
     * @param dictDTO 要插入的字典数据
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public SysDictData saveDictData(SysDictDTO dictDTO) {
        return saveDictDataPublic(dictDTO, false);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SysDictData saveDictDataOtherModule(SysDictDTO dictDTO) {
        return saveDictDataPublic(dictDTO, true);
    }

    /**
     * 保存数据词典公共方法
     *
     * @param dictDTO
     * @param otherServer
     * @return
     */
    private SysDictData saveDictDataPublic(SysDictDTO dictDTO, boolean otherServer) {
        SysDictData sysDictData = SysDictConvert.INSTANCE.convertDictData(dictDTO);

        String dataType = sysDictData.getDictType();
        // 校验同一类型下，字典名称是否重复,采用for update独占锁,保证新增加的字典值(dict_value)唯一
        List<SysDictData> dictDataList = baseMapper.findDictDataByType(dataType);
        int sort = 0;
        if (CollUtil.isNotEmpty(dictDataList)) {
            sort = dictDataList.size();
            Set<String> nameSet = dictDataList.stream().map(SysDictData::getDictName).collect(Collectors.toSet());
            if (otherServer) {
                if (nameSet.contains(dictDTO.getDictName())) {
                    SysDictData dictData = dictDataList.stream().filter(r -> r.getDictName().equals(sysDictData.getDictName())).findFirst().orElse(null);
                    if (Objects.nonNull(dictData)) {
                        sysDictData.setDictValue(dictData.getDictValue());
                        if (ApproveStatus.APPROVED.getCode().equals(sysDictData.getApproveStatus())) {
                            Long id = dictData.getId();
                            sysDictData.setId(id);
                            sysDictData.setDisable(false);
                            baseMapper.updateById(sysDictData);
                            this.cacheDictData(id);
                        }
                        return sysDictData;
                    }
                }
            } else {
                Assert.isFalse(nameSet.contains(dictDTO.getDictName()), "该字典类型下，字典名称: " + dictDTO.getDictName() + " 已存在");
            }
        }

        Map<String, String> dictMap = createDictValueByType(sort, dataType, sysDictData.getDictTypeName());
        sysDictData.setDictValue(dictMap.get("valueCode"));
        sysDictData.setSort(Integer.parseInt(dictMap.get("sort")));

        if (sysDictData.getApproveStatus() == null) {
            sysDictData.setApproveStatus(ApproveStatus.TO_BE_APPROVED.getCode());
        }
        sysDictData.setDisable(false);
        baseMapper.insert(sysDictData);

        if (ApproveStatus.APPROVED.getCode().equals(sysDictData.getApproveStatus())) {
            this.cacheDictData(sysDictData.getId());
        }
        return sysDictData;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateDictDataApproveStatus(Set<String> gaugeTypeSet, Integer approveStatus) {
        List<Long> dictDataIds = baseMapper.findDictDataByDictValues(gaugeTypeSet);
        //更新字典表审核状态
        baseMapper.updateApproveStatus(dictDataIds, approveStatus);

        if (ApproveStatus.APPROVED.getCode().equals(approveStatus)) {
            if (CollUtil.isNotEmpty(dictDataIds)) {
                dictDataIds.forEach(this::cacheDictData);
            }
        }
    }

    /**
     * 为新的字典数据创建编码和顺序号
     *
     * @param dictType 要插入的字典数据类型
     * @return Hashmap
     */
    private Map<String, String> createDictValueByType(int sort, String dictType, String dataTypeName) {
        StringBuilder valueCode = new StringBuilder();
        //得到每个单词首字母（为大写字母）的组合
        for (int i = 0; i < dictType.length(); i++) {
            if (Character.isUpperCase(dictType.charAt(i))) {
                valueCode.append(dictType.charAt(i));
            }
        }

        //顺序号
        if (sort == 0) {
            sysDictTypeMapper.insert(new SysDictType(dictType, dataTypeName));
        }

        String numberCoded;
        if (sort < NINE) {
            numberCoded = '0' + String.valueOf(sort + 1);
        } else {
            numberCoded = String.valueOf(sort + 1);
        }
        sort++;

        valueCode.append(numberCoded);

        HashMap<String, String> map = new HashMap<>(2);
        map.put("valueCode", valueCode.toString());
        map.put("sort", String.valueOf(sort));
        return map;
    }

    /**
     * 更新字典数据
     *
     * @param dictDTO 要更新的字典数据类型
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateDictData(SysDictDTO dictDTO) {
        SysDictData dictData = SysDictConvert.INSTANCE.convertDictData(dictDTO);
        baseMapper.updateById(dictData);

        if (ApproveStatus.APPROVED.getCode().equals(dictData.getApproveStatus())) {
            this.cacheDictData(dictData.getId());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SysDictData beforeUpdateValid(SysDictDTO dictDTO) {
        SysDictData dictData = SysDictConvert.INSTANCE.convertDictData(dictDTO);
        Assert.notNull(dictDTO.getId(), "数据ID不能为空");

        SysDictData beforeData = baseMapper.selectById(dictData.getId());
        Assert.notNull(beforeData, DATE_NOT_EXIST.getMessage());

        // 校验同一类型下，字典名称是否重复,采用for update独占锁,保证新增加的字典值(dict_value)唯一
        String dictType = beforeData.getDictType();
        String dictName = dictDTO.getDictName();
        if (StrUtil.isNotEmpty(dictName) && !dictData.getDictName().equals(beforeData.getDictName())) {
            List<SysDictData> dictDataList = baseMapper.findDictDataByType(dictType);
            if (CollUtil.isNotEmpty(dictDataList)) {
                Set<String> nameSet = dictDataList.stream().map(SysDictData::getDictName).collect(Collectors.toSet());
                Assert.isFalse(nameSet.contains(dictName), "该字典类型下，字典名称: " + dictName + " 已存在");
            }
        }
        return beforeData;
    }

    /**
     * 删除字典数据
     *
     * @param ids 要删除的id列表
     */
    @Override
    public List<BatchOperationTipDTO> deleteDictData(Set<Long> ids) {
        List<BatchOperationTipDTO> resultMap = new ArrayList<>();

        if (CollUtil.isNotEmpty(ids)) {
            for (Long id : ids) {
                SysDictData dictData = baseMapper.selectById(id);
                if (ObjectUtils.isEmpty(dictData)) {
                    resultMap.add(new BatchOperationTipDTO(id, "无效id"));
                    continue;
                }
                baseMapper.deleteById(id);

                RedisUtils.deleteCache(Constant.SYS_DICT_DATA_KEY + dictData.getDictValue());
                List<SysDictData> dictDataList = listDictData(dictData.getDictType(), false, ApproveStatus.APPROVED);
                if (CollUtil.isNotEmpty(dictDataList)) {
                    RedisUtils.cacheDictData(dictData.getDictType(), dictDataList);
                } else {
                    RedisUtils.deleteCache(Constant.SYS_DICT_KEY + dictData.getDictType());
                }
            }
        }

        return resultMap;
    }

    /**
     * 加载redis缓存，更新缓存数据
     *
     * @param id 字典数据ID
     */
    @Override
    public void cacheDictData(Long id) {
        SysDictData dictData = baseMapper.selectById(id);
        if (ObjectUtils.isNotEmpty(dictData) && !dictData.getDisable()) {
            RedisUtils.cacheDictData(SysDictConvert.INSTANCE.convertDictDTO(dictData));
            List<SysDictData> dictDataList = listDictData(dictData.getDictType(), false, ApproveStatus.APPROVED);
            RedisUtils.cacheDictData(dictData.getDictType(), dictDataList);
        }
    }

    /**
     * 分页查询数据词典
     *
     * @param query 查询条件
     * @return 分页数据
     */
    @Override
    public PageResult<SysDictDTO> listPage(DictNameReqVO query) {
        Page<SysDictDTO> page = new Page<>(query.getPageNum() != null ? query.getPageNum() : 1,
            query.getPageSize() != null ? Math.min(query.getPageSize(), CommonConstant.MAX_PAGE_SIZE) : CommonConstant.DEFAULT_PAGE_SIZE);
        List<SysDictData> models = baseMapper.findList(page, query);

        long total = page.getTotal();
        List<SysDictDTO> sysDict = SysDictConvert.INSTANCE.convertDTOList(models);
        return PageResult.<SysDictDTO>builder().data(sysDict).count(total).build();
    }

    @Override
    public List<String> getH5BannerList() {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            result.add(StrUtil.EMPTY);
        }
        return result;
    }
}
