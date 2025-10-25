package com.imd.db_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.Redisson;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.config.Config;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RedisCacheConfig {

    @Bean
    public RedissonReactiveClient redissonReactiveClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379");
        config.setCodec(new JsonJacksonCodec(new ObjectMapper()));

        return Redisson.create(config).reactive();
    }
}

