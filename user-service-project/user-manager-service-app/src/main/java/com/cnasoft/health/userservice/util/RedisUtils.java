package com.cnasoft.health.userservice.util;

import com.cnasoft.health.common.constant.RedisConstant;
import com.cnasoft.health.common.dto.SysAreaDTO;
import com.cnasoft.health.common.dto.SysDictDTO;
import com.cnasoft.health.common.dto.SysUserDTO;
import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.util.JsonUtils;
import com.cnasoft.health.redis.RedisRepository;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.convert.SysDictConvert;
import com.cnasoft.health.userservice.model.SysDictData;
import com.cnasoft.health.userservice.service.ISysAreaService;
import com.cnasoft.health.userservice.service.ISysDictService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 用户缓存工具类
 *
 * @author ganghe
 * @date 2022/4/10 22:51
 **/
@Slf4j
@Component
public class RedisUtils {
    private static RedisRepository redisRepository;
    private static ISysDictService dictService;
    private static ISysAreaService areaService;

    @Autowired
    public RedisUtils(RedisRepository redisRepository, ISysDictService dictService, ISysAreaService areaService) {
        RedisUtils.redisRepository = redisRepository;
        RedisUtils.dictService = dictService;
        RedisUtils.areaService = areaService;
    }

    public static Boolean existsKey(String key) {
        return redisRepository.exists(key);
    }

    /**
     * 从Redis中获取用户数据
     *
     * @param userId 登陆用户id
     * @return 用户对象
     */
    public static SysUserDTO getUserByCache(Long userId) {
        if (userId == null || userId == 0) {
            return null;
        }

        String userJson = redisRepository.get(RedisConstant.USER_SUFFIX + userId);
        if (StringUtils.isNotBlank(userJson)) {
            return JsonUtils.readValue(userJson, SysUserDTO.class);
        }
        return null;
    }

    /**
     * 缓存用户数据到Redis
     *
     * @param userDTO 用户对象
     */
    public static void cacheUser(SysUserDTO userDTO) {
        if (ObjectUtils.isNotEmpty(userDTO)) {
            userDTO.setPassword(null);
            redisRepository.set(RedisConstant.USER_SUFFIX + userDTO.getId(), JsonUtils.writeValueAsString(userDTO));
        }
    }

    /**
     * 缓存指定key的空字符串数据, ttl 300s
     *
     * @param key 键
     */
    public static void cacheEmpty(String key) {
        redisRepository.setEx(key, StringUtils.EMPTY, 300);
    }

    /**
     * 从Redis中获取区域数据
     *
     * @return 区域数据集合
     */
    public static SysAreaDTO getAreaByCache(Integer areaCode) {
        String areaJson = redisRepository.get(RedisConstant.AREA_SUFFIX + areaCode);
        if (StringUtils.isNotBlank(areaJson)) {
            return JsonUtils.readValue(areaJson, SysAreaDTO.class);
        } else {
            return areaService.getArea(areaCode);
        }
    }

    /**
     * 缓存区域数据到Redis
     *
     * @param areaDTO 区域数据
     */
    public static void cacheArea(SysAreaDTO areaDTO) {
        if (Objects.nonNull(areaDTO)) {
            redisRepository.set(RedisConstant.AREA_SUFFIX + areaDTO.getCode(), JsonUtils.writeValueAsString(areaDTO));
        }
    }

    /**
     * 缓存区域数据到Redis
     *
     * @param areaDTOList 区域数据集合
     */
    public static void cacheAllArea(Set<SysAreaDTO> areaDTOList) {
        if (CollectionUtils.isNotEmpty(areaDTOList)) {
            Map<String, String> map = new HashMap<>(1000);
            areaDTOList.forEach(area -> map.put(RedisConstant.AREA_SUFFIX + area.getCode(), JsonUtils.writeValueAsString(area)));
            redisRepository.executePipelined(map, -1);
        }
    }

    /**
     * 删除缓存数据
     *
     * @param key key
     */
    public static void deleteCache(String key) {
        if (StringUtils.isNotBlank(key) && redisRepository.exists(key)) {
            redisRepository.del(key);
        }
    }

    /**
     * 从缓存中获取数据词典数据
     *
     * @param dictType 字典类型
     * @return 数据词典列表
     */
    public static List<SysDictDTO> getDictData(String dictType) {
        if (StringUtils.isBlank(dictType)) {
            return Collections.emptyList();
        }

        String json = redisRepository.get(Constant.SYS_DICT_KEY + dictType);
        if (StringUtils.isNotBlank(json)) {
            return JsonUtils.readValue(json, new TypeReference<List<SysDictDTO>>() {
            });
        } else {
            return dictService.listDictDataByType(dictType, false, ApproveStatus.APPROVED);
        }
    }

    public static SysDictDTO getSingleDictData(String dictValue) {
        String json = redisRepository.get(Constant.SYS_DICT_DATA_KEY + dictValue);
        if (StringUtils.isNotBlank(json)) {
            return JsonUtils.readValue(json, SysDictDTO.class);
        }
        return null;
    }

    /**
     * 缓存数据字典数据到Redis
     *
     * @param dictData 字典数据
     */
    public static void cacheDictData(SysDictDTO dictData) {
        if (ObjectUtils.isNotEmpty(dictData)) {
            redisRepository.set(Constant.SYS_DICT_DATA_KEY + dictData.getDictValue(), JsonUtils.writeValueAsString(dictData));
        }
    }

    /**
     * 缓存数据字典数据到Redis
     *
     * @param dictType 字典类型
     * @param dictData 字典数据
     */
    public static void cacheDictData(String dictType, List<SysDictData> dictData) {
        if (CollectionUtils.isNotEmpty(dictData)) {
            redisRepository.set(Constant.SYS_DICT_KEY + dictType, JsonUtils.writeValueAsString(SysDictConvert.INSTANCE.convertDTOList(dictData)));
        }
    }
}
