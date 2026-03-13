package com.niu.community.cache.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheRebuildConfig {

    @Bean(name = "cacheRebuildExecutor")
    public Executor cacheRebuildExecutor() {
        return Executors.newFixedThreadPool(4);
    }
}