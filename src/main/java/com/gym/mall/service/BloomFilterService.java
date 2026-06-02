package com.gym.mall.service;

public interface BloomFilterService {

    boolean mightContainCommodity(Long commodityId);

    boolean mightContainUser(Long userId);

    void addCommodity(Long commodityId);

    void addUser(Long userId);

    void initCommodityBloomFilter();

    void initUserBloomFilter();
}
