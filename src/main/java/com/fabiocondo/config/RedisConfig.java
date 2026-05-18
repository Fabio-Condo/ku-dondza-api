package com.fabiocondo.config;

import com.fabiocondo.constant.CacheNames;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    // 1. Conexão Redis
    @Bean
    public RedisStandaloneConfiguration redisStandaloneConfiguration() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        return config;
    }

    // 2. Configuração base de cache
    private RedisCacheConfiguration baseConfig() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer())
                );
    }

    // 3. Cache Manager com TTL por tipo (IMPORTANTE)
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        // SUBJECTS
        cacheConfigs.put(CacheNames.SUBJECT_LIST,
                baseConfig().entryTtl(Duration.ofMinutes(3)));

        // EXAMS
        cacheConfigs.put(CacheNames.EXAM_FILTER,
                baseConfig().entryTtl(Duration.ofMinutes(3)));

        // QUIZZES
        cacheConfigs.put(CacheNames.QUIZ_FILTER,
                baseConfig().entryTtl(Duration.ofMinutes(3)));
        cacheConfigs.put(CacheNames.QUIZ_DETAILS,
                baseConfig().entryTtl(Duration.ofMinutes(3)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(baseConfig().entryTtl(Duration.ofMinutes(10))) // fallback
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}