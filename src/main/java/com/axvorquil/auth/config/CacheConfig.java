package com.axvorquil.auth.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
            build("systemHealth",  30),    // 30s — health aggregator
            build("orgData",      300),    // 5 min — org info
            build("billingPlans", 3600),   // 1 hour — billing plans (rarely change)
            build("users",        120)     // 2 min — user list
        ));
        return manager;
    }

    private CaffeineCache build(String name, int ttlSeconds) {
        return new CaffeineCache(name, Caffeine.newBuilder()
            .expireAfterWrite(ttlSeconds, TimeUnit.SECONDS)
            .maximumSize(500)
            .build());
    }
}
