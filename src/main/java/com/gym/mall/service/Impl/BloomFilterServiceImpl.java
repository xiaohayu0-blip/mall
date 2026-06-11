package com.gym.mall.service.Impl;

import com.gym.mall.Repository.CommodityRepository;
import com.gym.mall.Repository.UserRepository;
import com.gym.mall.domain.entity.Commodity;
import com.gym.mall.domain.entity.User;
import com.gym.mall.service.BloomFilterService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class BloomFilterServiceImpl implements BloomFilterService {

    @Autowired
    private RBloomFilter<Long> commodityBloomFilter;

    @Autowired
    private RBloomFilter<Long> userBloomFilter;

    @Autowired
    private CommodityRepository commodityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Override
    public boolean mightContainCommodity(Long commodityId) {
        if (commodityId == null) {
            return false;
        }
        return commodityBloomFilter.contains(commodityId);
    }

    @Override
    public boolean mightContainUser(Long userId) {
        if (userId == null) {
            return false;
        }
        return userBloomFilter.contains(userId);
    }

    @Override
    public void addCommodity(Long commodityId) {
        if (commodityId != null) {
            commodityBloomFilter.add(commodityId);
            log.debug("添加商品ID到布隆过滤器: {}", commodityId);
        }
    }

    @Override
    public void addUser(Long userId) {
        if (userId != null) {
            userBloomFilter.add(userId);
            log.debug("添加用户ID到布隆过滤器: {}", userId);
        }
    }

    @PostConstruct
    public void init() {
        try {
            initCommodityBloomFilter();
            initUserBloomFilter();
        } catch (Exception e) {
            log.warn("布隆过滤器初始化跳过（数据库表尚未就绪，Flyway 将在后续建表）: {}", e.getMessage());
        }
    }


    @Override
    public void initCommodityBloomFilter() {
        log.info("开始初始化商品布隆过滤器...");

        List<Long> commodityIds = commodityRepository.findAll().stream()
                .map(Commodity::getId)
                .toList();

        commodityBloomFilter.clearExpire();

        for (Long id : commodityIds) {
            commodityBloomFilter.add(id);
        }

        log.info("商品布隆过滤器初始化完成，共加载 {} 个商品", commodityIds.size());
    }


    @Override
    public void initUserBloomFilter() {
        log.info("开始初始化用户布隆过滤器...");

        List<Long> userIds = userRepository.findAll().stream()
                .map(User::getUserId)
                .toList();

        userBloomFilter.clearExpire();

        for (Long id : userIds) {
            userBloomFilter.add(id);
        }

        log.info("用户布隆过滤器初始化完成，共加载 {} 个用户", userIds.size());
    }
}
