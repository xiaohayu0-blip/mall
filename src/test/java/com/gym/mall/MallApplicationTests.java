package com.gym.mall;

import com.gym.mall.service.CommoditySearchService;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.amqp.autoconfigure.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.boot.data.redis.autoconfigure.DataRedisReactiveAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {
        RabbitAutoConfiguration.class,
        DataRedisAutoConfiguration.class,
        DataRedisReactiveAutoConfiguration.class
})
class MallApplicationTests {

    @MockitoBean
    private RedissonClient redissonClient;

    @MockitoBean
    private ConnectionFactory rabbitConnectionFactory;

    @MockitoBean
    @SuppressWarnings("rawtypes")
    private RBloomFilter commodityBloomFilter;

    @MockitoBean
    @SuppressWarnings("rawtypes")
    private RBloomFilter userBloomFilter;

    @MockitoBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @MockitoBean
    private CommoditySearchService commoditySearchService;

    @Test
    void contextLoads() {
    }
}
