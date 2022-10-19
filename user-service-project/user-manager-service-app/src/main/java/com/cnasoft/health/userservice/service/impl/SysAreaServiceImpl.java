package com.cnasoft.health.userservice.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cnasoft.health.common.constant.RedisConstant;
import com.cnasoft.health.common.dto.BatchOperationTipDTO;
import com.cnasoft.health.common.dto.SysAreaDTO;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.enums.AreaTypeEnum;
import com.cnasoft.health.common.service.impl.SuperServiceImpl;
import com.cnasoft.health.common.vo.PageResult;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.convert.SysAreaConvert;
import com.cnasoft.health.userservice.mapper.SysAreaMapper;
import com.cnasoft.health.userservice.model.SysArea;
import com.cnasoft.health.userservice.service.IAreaStaffService;
import com.cnasoft.health.userservice.service.IAreaTeacherService;
import com.cnasoft.health.userservice.service.ISchoolService;
import com.cnasoft.health.userservice.service.ISysAreaService;
import com.cnasoft.health.userservice.service.ISysUserService;
import com.cnasoft.health.userservice.util.AreaUtil;
import com.cnasoft.health.userservice.util.RedisUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.cnasoft.health.userservice.constant.UserErrorCodeConstants.AREA_NOT_EXISTS;

/**
 * 区域管理
 *
 * @author ganghe
 */
@Service
public class SysAreaServiceImpl extends SuperServiceImpl<SysAreaMapper, SysArea> implements ISysAreaService {

    /**
     * 省
     */
    private static final int TYPE_PROVINCE = 0;

    /**
     * 市
     */
    private static final int TYPE_CITY = 1;

    /**
     * 区
     */
    private static final int TYPE_AREA = 2;

    private static final String FOUR = "0000";

    private static final String TWO = "00";

    private static final int FOUR_NUMBER = 4;

    @Resource
    private AreaUtil areaUtil;

    @Resource
    public TaskExecutor taskExecutor;

    @Resource
    IAreaTeacherService areaTeacherService;

    @Resource
    IAreaStaffService areaStaffService;

    @Resource
    ISchoolService schoolService;

    @Resource
    ISysUserService sysUserService;

    /**
     * 通过自定义数据查询区域列表
     *
     * @param params 参数列表
     * @return 数据集
     */
    @Override
    public Set<SysAreaDTO> getAreaList(Map<String, Object> params) {
        List<SysAreaDTO> allAreaList = baseMapper.getAreaList(params);
        List<SysAreaDTO> provinceList = getTreeData(allAreaList);
        return new HashSet<>(provinceList);
    }

    @Override
    public PageResult<SysAreaDTO> getAreaListPage(Map<String, Object> params) {
        Page<SysAreaDTO> page = new Page<>(MapUtil.getInt(params, Constant.PAGE_NUM, 1), MapUtil.getInt(params, Constant.PAGE_SIZE, 10));
        List<SysAreaDTO> result = baseMapper.getAreaList(page, params);
        long total = page.getTotal();

        for (SysAreaDTO areaDTO : result) {
            getAreaNameInfo(areaDTO);
        }

        return PageResult.<SysAreaDTO>builder().data(result).count(total).build();
    }

    @Override
    public void getAreaNameInfo(SysAreaDTO areaDTO) {

        Integer code = areaDTO.getCode();
        if (Objects.isNull(code) || code.toString().length() != 6) {
            return;
        }

        int type = getType(code);
        if (type == TYPE_PROVINCE) {
            areaDTO.setProvince(areaDTO.getCode());
            areaDTO.setProvinceName(areaDTO.getName());
        } else {
            SysAreaDTO province = areaUtil.getArea(code / 10000 * 10000);
            if (Objects.isNull(province)) {
                province = SysAreaConvert.INSTANCE.convert(baseMapper.selectByCode(code));
            }
            if (Objects.nonNull(province)) {
                areaDTO.setProvince(province.getCode());
                areaDTO.setProvinceName(province.getName());
            }
        }
        if (type == TYPE_CITY) {
            areaDTO.setCity(code);
            areaDTO.setCityName(areaDTO.getName());
        } else if (type == TYPE_AREA) {
            SysAreaDTO city = areaUtil.getArea(code / 100 * 100);
            if (Objects.isNull(city)) {
                city = SysAreaConvert.INSTANCE.convert(baseMapper.selectByCode(code));
            }
            if (Objects.nonNull(city)) {
                areaDTO.setCity(city.getCode());
                areaDTO.setCityName(city.getName());
            }
            areaDTO.setDistinct(code);
            areaDTO.setDistinctName(areaDTO.getName());
        }
    }

    @Override
    public void cacheAllArea() {
        List<SysAreaDTO> allAreaList = baseMapper.getAreaList(null);
        RedisUtils.cacheAllArea(new HashSet<>(allAreaList));
    }

    private List<SysAreaDTO> getTreeData(List<SysAreaDTO> allAreaList) {
        List<SysAreaDTO> provinceList = allAreaList.stream().filter(area -> area.getType() == 0).collect(Collectors.toList());
        for (SysAreaDTO provinceArea : provinceList) {
            List<SysAreaDTO> cityList =
                allAreaList.stream().filter(area -> area.getType() == 1 && area.getCode().toString().startsWith(provinceArea.getCode().toString().substring(0, 2)))
                    .collect(Collectors.toList());

            for (SysAreaDTO cityArea : cityList) {
                List<SysAreaDTO> areaList =
                    allAreaList.stream().filter(area -> area.getType() == 2 && area.getCode().toString().startsWith(cityArea.getCode().toString().substring(0, 4)))
                        .collect(Collectors.toList());
                cityArea.setSubAreaList(new HashSet<>(areaList));
            }

            provinceArea.setSubAreaList(new HashSet<>(cityList));
        }

        return provinceList;
    }

    public void updateAreaCache(Long id) {
        SysArea area = baseMapper.selectById(id);
        if (Objects.nonNull(area)) {
            RedisUtils.cacheArea(SysAreaConvert.INSTANCE.convert(area));
        }
    }

    @Override
    public SysArea saveArea(SysArea area) {
        Assert.isTrue(area.getCode().toString().length() == 6, "区域编码必须6位");

        area.setType(getType(area.getCode()));
        SysArea exist = baseMapper.selectOne(new LambdaQueryWrapper<SysArea>().eq(SysArea::getName, area.getName()));
        Assert.isNull(exist, "区域名称已存在");
        exist = baseMapper.selectOne(new LambdaQueryWrapper<SysArea>().eq(SysArea::getCode, area.getCode()));
        Assert.isNull(exist, "区域编码已存在");
        int count = baseMapper.insert(area);
        if (count > 0 && ApproveStatus.APPROVED.getCode().equals(area.getApproveStatus())) {
            // 更新区域缓存数据
            updateAreaCache(area.getId());
        }
        return area;
    }

    @Override
    public void updateArea(SysArea area) {
        SysArea oldArea = beforeUpdateValid(area);
        if (!oldArea.getCode().equals(area.getCode())) {
            RedisUtils.deleteCache(RedisConstant.AREA_SUFFIX + oldArea.getCode());
        }

        baseMapper.updateById(area);
        if (ApproveStatus.APPROVED.getCode().equals(area.getApproveStatus())) {
            // 更新区域缓存数据
            updateAreaCache(area.getId());
        }
    }

    @Override
    public SysArea beforeUpdateValid(SysArea area) {
        Assert.isTrue(area.getCode().toString().length() == 6, "区域编码必须6位");

        SysArea oldArea = baseMapper.selectById(area.getId());
        Assert.notNull(oldArea, AREA_NOT_EXISTS.getMessage());

        SysArea exist = baseMapper.selectOne(new LambdaQueryWrapper<SysArea>().eq(SysArea::getName, area.getName()));
        if (Objects.nonNull(exist)) {
            Assert.isTrue(exist.getId().equals(area.getId()), "区域名称已存在");
        }

        exist = baseMapper.selectOne(new LambdaQueryWrapper<SysArea>().eq(SysArea::getCode, area.getCode()));
        if (Objects.nonNull(exist)) {
            Assert.isTrue(exist.getId().equals(area.getId()), "区域编码已存在");
        }
        return oldArea;
    }

    @Override
    public void updateEnabled(Long id, Boolean enabled) {
        SysArea oldArea = baseMapper.selectById(id);
        Assert.notNull(oldArea, AREA_NOT_EXISTS.getMessage());

        SysArea area = new SysArea();
        area.setId(id);
        area.setEnabled(enabled);
        baseMapper.updateById(area);

        // 更新区域缓存数据
        updateAreaCache(area.getId());
    }

    @Override
    public List<BatchOperationTipDTO> delete(Set<Long> ids) {
        List<BatchOperationTipDTO> resultMap = new ArrayList<>();

        //获取所有下级区域id
        Set<Long> allIds = new HashSet<>();
        for (Long id : ids) {
            SysArea area = baseMapper.selectById(id);
            if (ObjectUtils.isEmpty(area)) {
                resultMap.add(new BatchOperationTipDTO(id, "无效id"));
                continue;
            }
            allIds.add(id);
            if (AreaTypeEnum.AREA_PROVINCE.getCode().equals(area.getType())) {
                String provinceCode = area.getCode().toString().substring(0, 2);
                allIds.addAll(baseMapper.getAreaIdByProvince(provinceCode));
            } else if (AreaTypeEnum.AREA_CITY.getCode().equals(area.getType())) {
                String cityCode = area.getCode().toString().substring(0, 4);
                allIds.addAll(baseMapper.getAreaIdByCity(cityCode));
            }
        }

        if (CollectionUtils.isNotEmpty(allIds)) {
            for (Long id : allIds) {
                SysArea area = baseMapper.selectById(id);
                if (ObjectUtils.isEmpty(area)) {
                    continue;
                }
                baseMapper.deleteById(id);

                Integer areaCode = area.getCode();
                //删除区域职员
                areaStaffService.deleteByAreaCode(areaCode);
                //删除区域心理教师
                areaTeacherService.deleteByAreaCode(areaCode);
                //删除区域下的学校
                schoolService.deleteByAreaCode(areaCode);
                //删除用户
                sysUserService.deleteByAreaCode(areaCode);
                RedisUtils.deleteCache(RedisConstant.AREA_SUFFIX + areaCode);
            }
        }

        return resultMap;
    }

    @Override
    public Date getLastUpdateTime() {
        return baseMapper.selectLastUpdateTime();
    }

    @Override
    public Boolean getAreaAvailableStatus(Integer areaCode) {
        SysArea area = baseMapper.selectOne(new LambdaQueryWrapper<SysArea>().eq(SysArea::getCode, areaCode));
        if (Objects.isNull(area)) {
            return Boolean.FALSE;
        }

        return area.getEnabled();
    }

    private int getType(Integer code) {
        int type = TYPE_AREA;
        String codeStr = code.toString();
        if (codeStr.startsWith(FOUR, codeStr.length() - FOUR_NUMBER)) {
            type = TYPE_PROVINCE;
        } else if (codeStr.startsWith(TWO, codeStr.length() - TYPE_AREA)) {
            type = TYPE_CITY;
        }

        return type;
    }

    @Override
    public SysAreaDTO getArea(Integer areaCode) {
        SysArea area = baseMapper.selectOne(Wrappers.lambdaQuery(SysArea.class).eq(SysArea::getCode, areaCode).eq(SysArea::getEnabled, true));
        return SysAreaConvert.INSTANCE.convert(area);
    }
}
