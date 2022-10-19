package com.cnasoft.health.userservice.config;

import com.cnasoft.health.common.enums.ApproveStatus;
import com.cnasoft.health.common.util.JsonUtils;
import com.cnasoft.health.redis.RedisRepository;
import com.cnasoft.health.userservice.constant.Constant;
import com.cnasoft.health.userservice.constant.RedisDataSource;
import com.cnasoft.health.userservice.convert.SysDictConvert;
import com.cnasoft.health.userservice.model.SysDictData;
import com.cnasoft.health.userservice.model.SysDictType;
import com.cnasoft.health.userservice.service.ISysAreaService;
import com.cnasoft.health.userservice.service.ISysDictService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author ganghe
 */
@Slf4j
@Component
public class ApplicationStartedListener implements ApplicationListener<ApplicationStartedEvent> {
    @Qualifier(RedisDataSource.USERSENTINEL_SOURCE)
    @Resource
    private RedisRepository redisRepository;

    @Resource
    private ISysDictService sysDictService;

    @Resource
    private ISysAreaService sysAreaService;

    @Override
    public void onApplicationEvent(@NonNull ApplicationStartedEvent applicationStartedEvent) {
        //父容器启动时执行该方法,避免该方法执行两次的问题
        if (Objects.requireNonNull(applicationStartedEvent.getApplicationContext().getParent()).getParent() == null) {
            String initData = redisRepository.get("init_data");
            if (StringUtils.isNotBlank(initData)) {
                return;
            }

            //查询数据词典信息缓存到Redis
            List<SysDictType> dictTypeList = sysDictService.listDictType();
            if (CollectionUtils.isNotEmpty(dictTypeList)) {
                for (SysDictType sysDictType : dictTypeList) {
                    List<SysDictData> dictDataList = sysDictService.listDictData(sysDictType.getDictTypeCode(), false, ApproveStatus.APPROVED);
                    if (dictDataList != null && !dictDataList.isEmpty()) {
                        redisRepository.set(Constant.SYS_DICT_KEY + sysDictType.getDictTypeCode(),
                            JsonUtils.writeValueAsString(SysDictConvert.INSTANCE.convertDTOList(dictDataList)));

                        //缓存每条字典数据
                        for (SysDictData dictData : dictDataList) {
                            redisRepository.set(Constant.SYS_DICT_DATA_KEY + dictData.getDictValue(),
                                JsonUtils.writeValueAsString(SysDictConvert.INSTANCE.convertDictDTO(dictData)));
                        }
                    }
                }
            }

            //查询区域数据缓存到Redis
            sysAreaService.cacheAllArea();

            redisRepository.set("init_data", "finish");
        }
    }
}
