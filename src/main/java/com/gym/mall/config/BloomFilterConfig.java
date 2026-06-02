package com.gym.mall.config;

import com.gym.mall.BloomFilterConstants;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class BloomFilterConfig {

    @Bean
    public RBloomFilter<Long> commodityBloomFilter(RedissonClient redissonClient) {
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(
                BloomFilterConstants.COMMODITY_BLOOM_FILTER);

        boolean success = bloomFilter.tryInit(
                BloomFilterConstants.DEFAULT_EXPECTED_INSERTIONS,
                BloomFilterConstants.DEFAULT_FPP
        );

        if (success) {
            log.info("商品布隆过滤器初始化成功，预期插入量: {}, 误判率: {}",
                    BloomFilterConstants.DEFAULT_EXPECTED_INSERTIONS,
                    BloomFilterConstants.DEFAULT_FPP);
        } else {
            log.warn("商品布隆过滤器已存在，跳过初始化");
        }

        return bloomFilter;
    }

    @Bean
    public RBloomFilter<Long> userBloomFilter(RedissonClient redissonClient) {
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(
                BloomFilterConstants.USER_BLOOM_FILTER);

        boolean success = bloomFilter.tryInit(
                BloomFilterConstants.DEFAULT_EXPECTED_INSERTIONS,
                BloomFilterConstants.DEFAULT_FPP
        );

        if (success) {
            log.info("用户布隆过滤器初始化成功");
        } else {
            log.warn("用户布隆过滤器已存在，跳过初始化");
        }

        return bloomFilter;
     }
}
