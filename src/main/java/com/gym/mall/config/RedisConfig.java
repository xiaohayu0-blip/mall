package com.gym.mall.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    /**
     * 配置 RedisTemplate，用于在 Java 代码中操作 Redis
     * @param factory Redis 连接工厂，Spring 会自动注入
     * @return 配置好的 RedisTemplate 实例
     */
    @Bean("redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        // 创建 RedisTemplate 实例
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        
        // 设置连接工厂，让 Template 知道连接哪个 Redis 服务器
        redisTemplate.setConnectionFactory(factory);
        
        // 设置 Key 的序列化器：使用字符串序列化器，这样在 Redis 客户端看 key 是正常的字符串
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        
        // 设置 Value 的序列化器：使用 JSON 序列化器，它可以自动将 Java 对象转换为 JSON 字符串存入 Redis
        // 这样我们存入 Object 类型时，取出来也是对应的对象，非常方便
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        return redisTemplate;
    }
}
