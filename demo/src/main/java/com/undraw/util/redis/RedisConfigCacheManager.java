package com.undraw.util.redis;

import cn.undraw.util.ConvertUtils;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;

import java.time.Duration;

public class RedisConfigCacheManager extends RedisCacheManager {

    public RedisConfigCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration) {
        super(cacheWriter, defaultCacheConfiguration);
    }

    @Override
    protected RedisCache createRedisCache(String name, RedisCacheConfiguration cacheConfig) {
        int lastIndex;
        if (name != null && (lastIndex = name.lastIndexOf("#")) != -1) {
            try {
                String ttl = name.substring(lastIndex + 1);
                cacheConfig  = cacheConfig.entryTtl(Duration.ofSeconds(ConvertUtils.toLong(ttl)));
                name = name.substring(0, lastIndex);
                return super.createRedisCache(name, cacheConfig);
            } catch (Exception e) {
            }
        }
        return super.createRedisCache(name, cacheConfig);
    }


}
