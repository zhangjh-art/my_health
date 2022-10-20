package com.cnasoft.health.auth.config;

import cn.hutool.core.util.StrUtil;
import com.cnasoft.health.auth.bloom.BloomFilter;
import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.userservice.feign.UserFeignClient;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ganghe
 */
@Slf4j
@Component
public class ApplicationStartedListener implements ApplicationListener<ApplicationStartedEvent> {

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private TaskExecutor taskExecutor;

    @Resource
    private BloomFilter bloomFilter;

    public void addUserToFilter(Collection<String> keys) {
        keys.remove(StrUtil.EMPTY);
        keys.remove(null);
        keys.forEach(key -> this.bloomFilter.add(key));
    }

    @SneakyThrows
    @Override
    public void onApplicationEvent(@NonNull ApplicationStartedEvent applicationStartedEvent) {
        //父容器启动时执行该方法,避免该方法执行两次的问题
        if (Objects.requireNonNull(applicationStartedEvent.getApplicationContext().getParent()).getParent() == null) {
            long totalBegin = System.currentTimeMillis();
            CommonResult<Long> countData = userFeignClient.findUserCount();
            countData.checkError();
            log.info("预计缓存用户数量：{}", countData.getData());

            AtomicInteger total = new AtomicInteger();
            int limit = 1000;
            int offset = 0;
            int count = countData.getData().intValue();
            if (count > limit) {
                int times = (count + limit - 1) / limit;
                log.info("批量查询{}次", times);

                taskExecutor.execute(() -> {
                    for (int i = 0; i < times; i++) {
                        try {
                            CommonResult<List<String>> result = userFeignClient.findUsernameAndMobileOrShortId(limit, i * limit + offset);
                            result.checkError();

                            List<String> keys = result.getData();
                            total.addAndGet(keys.size());

                            addUserToFilter(keys);
                        } catch (Exception e) {
                            log.error("findUsernameAndMobileOrShortId exception", e);
                        }
                    }
                    log.info("成功加载数据{}条，用时{}秒", total.get(), (System.currentTimeMillis() - totalBegin) / 1000);
                    BloomFilter.setLoadUser(true);
                });
            } else {
                CommonResult<List<String>> result = userFeignClient.findUsernameAndMobileOrShortId(limit, offset);
                result.checkError();

                List<String> keys = result.getData();

                addUserToFilter(keys);

                log.info("成功加载数据{}条，用时{}秒", total.get(), (System.currentTimeMillis() - totalBegin) / 1000);
                BloomFilter.setLoadUser(true);
            }
        }
    }
}
