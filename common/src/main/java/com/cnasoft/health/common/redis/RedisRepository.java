package com.cnasoft.health.common.redis;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 面向开发者的Redis操作工具。与redis官方命令保持同步。
 * 以redis官方命令封装redis常用操作， 简化业务调用，方便统一维护。
 * 只支持StringRedisTemplate，如果牵涉到对象的编解码请在业务程序实现。
 *
 * @author cnasoft
 * @date 2020/5/26 9:30
 */
@Slf4j
public class RedisRepository {

    private StringRedisTemplate redisTemplate;

    public RedisRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void setEx(String key, String value, int timeout) {
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(timeout));
    }

    public Boolean setNx(String key, String value) {
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    public Boolean setNxEx(String key, String value, int timeout) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, Duration.ofSeconds(timeout));
    }

    public Long ttl(String key) {
        return redisTemplate.opsForValue().getOperations().getExpire(key);
    }

    public boolean del(String key) {
        return redisTemplate.delete(key);
    }

    public void hset(String key, String hashKey, String hashValue) {
        opsForHash().put(key, hashKey, hashValue);
    }

    public String hget(String key, String hashKey) {
        return opsForHash().get(key, hashKey);
    }

    public List<String> hMultiGet(String key, List<String> hashKeys) {
        return opsForHash().multiGet(key, hashKeys);
    }

    public Map<String, String> hgetAll(String key) {
        return opsForHash().entries(key);
    }

    public Long hdel(String key, String... hashKeys) {
        return opsForHash().delete(key, hashKeys);
    }

    public HashOperations<String, String, String> opsForHash() {
        return redisTemplate.opsForHash();
    }

    public ListOperations<String, String> opsForList() {
        return redisTemplate.opsForList();
    }

    public SetOperations<String, String> opsForSet() {
        return redisTemplate.opsForSet();
    }

    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    //NONE("none"), STRING("string"), LIST("list"), SET("set"), ZSET("zset"), HASH("hash");
    public DataType type(String key) {
        return redisTemplate.type(key);
    }

    public List<String> lrange(final String key, final long start, final long stop) {
        return opsForList().range(key, start, stop);
    }

    public long llen(String key) {
        return opsForList().size(key);
    }

    public Set<String> smembers(String key) {
        return opsForSet().members(key);
    }

    public Boolean exists(String key) {
        return redisTemplate.opsForValue().getOperations().hasKey(key);
    }

    public void incr(String key) {
        redisTemplate.opsForValue().increment(key);
    }

    public long incrAndGet(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    public void decr(String key) {
        redisTemplate.opsForValue().decrement(key);
    }

    public void expire(String key, long timeout) {
        redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }

    public void expireAt(String key, Date date) {
        redisTemplate.expireAt(key, date);
    }

    public void rpush(String key, String value) {
        opsForList().rightPush(key, value);
    }

    public void setAdd(String key, String value) {
        opsForSet().add(key, value);
    }

    public String setPop(String key) {
        return opsForSet().pop(key);
    }

    public long setSize(String key) {
        return opsForSet().size(key);
    }

    public String lindex(String key, long index) {
        return opsForList().index(key, index);
    }

    /**
     * 批量插入
     *
     * @param map     Map<String, String>
     * @param seconds 过期时间（秒）
     */
    public int executePipelined(Map<String, String> map, long seconds) {
        RedisSerializer keySerializer = redisTemplate.getKeySerializer();
        RedisSerializer valueSerializer = redisTemplate.getValueSerializer();

        List<Object> list = redisTemplate.executePipelined((RedisCallback<String>) connection -> {
            Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> next = iterator.next();
                if (Objects.nonNull(next.getKey()) && Objects.nonNull(next.getValue())) {
                    connection.set(keySerializer.serialize(next.getKey()), valueSerializer.serialize(next.getValue()), Expiration.seconds(seconds), RedisStringCommands.SetOption.UPSERT);
                }
            }
            return null;
        }, valueSerializer);

        if (CollectionUtils.isNotEmpty(list)) {
            return list.size();
        }
        return 0;
    }
}
