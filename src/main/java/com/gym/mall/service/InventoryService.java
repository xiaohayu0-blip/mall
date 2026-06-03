package com.gym.mall.service;

import java.util.List;
import java.util.Map;

public interface InventoryService {

    /**
     * 批量扣减库存（分布式锁 + Lua 原子操作）
     *
     * @param commodityMap 商品ID -> 扣减数量
     * @return true=扣减成功，false=库存不足
     */
    boolean deductStock(Map<Long, Integer> commodityMap);

    /**
     * 回退库存（取消订单时使用）
     *
     * @param commodityMap 商品ID -> 回退数量
     */
    void restoreStock(Map<Long, Integer> commodityMap);
}
