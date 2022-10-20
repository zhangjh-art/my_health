package com.cnasoft.health.auth.bloom;

import com.cnasoft.health.common.vo.CommonResult;
import com.cnasoft.health.userservice.feign.UserFeignClient;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @Created by lgf on 2022/4/12.
 */
@Slf4j
@Component
public class BloomFilter {

    @Resource(name = "auth-token-sentinel-string-redis-template")
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserFeignClient userFeignClient;

    /**
     * BKRD Hash算法seed
     */
    private Integer[] seed;

    private HashFunction[] hashFunction;

    private Long cap;

    private static final String BIT_MAP_PREFIX = "authBloom:bitMapForBloomFilter";

    /**
     * 过滤器误判概率
     */
    private static final double MISJUDGMENT_PROBABILITY = 0.0001;

    /**
     * 加载用户到布隆过滤器是否完成
     */
    @Setter
    @Getter
    private static Boolean loadUser;

    /**
     * 初始化
     * 参考 https://www.cnblogs.com/liyulong1982/p/6013002.html
     * 对于给定误判概率p以及需要插入元素总数n，布隆过滤器大小最优为：m = -n*ln(p)/(ln(2))^2
     * 对于给定位数组大小 m 以及插入元素个数 n，hash函数最优数目为：k = (m/n)*(ln(2))
     */
    @PostConstruct
    public BloomFilter getBloomFilter() throws InterruptedException {
        flush();

        loadUser = false;

        long n = getUserCount() * 2;
        // 槽数量
        int m = (int) ((-1) * n * Math.log(MISJUDGMENT_PROBABILITY) / Math.sqrt(Math.log(2)));
        // hash函数数量
        int k = (int) ((m / n) * (Math.log(2)));

        CorrectRatio correctRatio = new CorrectRatio(k);
        initCap(correctRatio, n);
        createHashFunctions(correctRatio);

        log.info("cap:{}", this.cap);
        log.info("seed length:{}", this.seed.length);
        return this;
    }

    /**
     * 初始化容量 redis中位图的容量
     *
     * @param n 输入量
     */
    private void initCap(CorrectRatio correctRatio, Long n) {

        int pow = (int) (Math.log(n) / Math.log(2)) + 1;

        /*
         * 如果correctRatio.getSeed().length 为32 即有32个hash函数对一个数做映射
         * 那么 bitSet中 32位代表一个数 所需要的最大容量为32*n
         */
        this.cap = (long) (correctRatio.getSeed().length * (1 << pow));
    }

    /**
     * 构造 hash函数
     *
     * @param correctRatio correctRatio
     */
    private void createHashFunctions(CorrectRatio correctRatio) {
        this.seed = correctRatio.getSeed();
        this.hashFunction = new HashFunction[this.seed.length];
        for (int i = 0; i < seed.length; i++) {
            this.hashFunction[i] = new HashFunction(seed[i], this.cap);
        }
    }

    /**
     * hash算法
     */
    private static class HashFunction {
        private final Integer seed;

        private final Long cap;

        public HashFunction(Integer seed, Long cap) {
            this.seed = seed;
            this.cap = cap;
        }

        public Long hash(String value) {
            int result = 0;
            for (int i = 0; i < value.length(); i++) {
                result = result * this.seed + value.charAt(i);
            }
            // 求余数 防止redis中bitMap无限膨胀 类似循环队列 可以防止系统OOM
            return result & (cap - 1);
        }
    }

    private long getUserCount() throws InterruptedException {
        int retrySecond = 10;
        Long count = 0L;
        while (count <= 0) {
            try {
                CommonResult<Long> feignResult = userFeignClient.findUserCount();
                feignResult.checkError();
                count = feignResult.getData();
            } catch (Exception e) {
                log.error(e.getMessage());
                log.error("用户模块调用失败，{}s后重试...", retrySecond);
                Thread.sleep(retrySecond * 1000);
            }
        }

        return count;
    }

    /**
     * 添加
     *
     * @param value value
     */
    public void add(String value) {
        for (int i = 0; i < seed.length; i++) {
            this.stringRedisTemplate.opsForValue().setBit(BIT_MAP_PREFIX, this.hashFunction[i].hash(value), true);
        }
    }

    /**
     * 包含
     *
     * @param value value
     * @return boolean
     */
    public boolean mightContain(String value) {
        if (Objects.isNull(value)) {
            return false;
        }

        //当布隆过滤器的key不存在时，则不限制
        if (!stringRedisTemplate.opsForValue().getOperations().hasKey(BIT_MAP_PREFIX)) {
            return true;
        }

        Boolean flag;
        for (int i = 0; i < seed.length; i++) {
            flag = this.stringRedisTemplate.opsForValue().getBit(BIT_MAP_PREFIX, this.hashFunction[i].hash(value));
            if (flag == null || !flag) {
                return false;
            }
        }
        return true;
    }

    /**
     * 清空过滤器所有数据
     */
    private void flush() {
        this.stringRedisTemplate.expire(BIT_MAP_PREFIX, 0, TimeUnit.SECONDS);
    }
}

