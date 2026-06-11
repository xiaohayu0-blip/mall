package com.gym.mall.service.Impl;

import com.gym.mall.Repository.CommodityRepository;
import com.gym.mall.domain.entity.Commodity;
import com.gym.mall.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.gym.mall.Constants.STOCK_LOCK_KEY;

/**
 * 库存服务 — Redisson 分布式锁实现
 *
 * 知识点：
 * 1. 分布式锁（Redisson）：避免高并发下的超卖问题
 * 2. WatchDog 自动续期：防止锁超时导致数据不一致
 * 3. 有序加锁：按 commodityId 排序避免死锁
 * 4. 事务回滚：异常时自动回滚数据库操作
 *
 * 为什么不用数据库乐观锁？
 * - 乐观锁（version）在冲突频繁时会导致大量重试，性能反而更差
 * - 分布式锁直接串行化对同一商品的库存操作，更适合电商秒杀场景
 */
@Service
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private CommodityRepository commodityRepository;

    /**
     * 扣减库存
     * @param commodityMap 商品ID -> 扣减数量
     * @return
     */
    @Override
    @Transactional
    public boolean deductStock(Map<Long, Integer> commodityMap) {
        if (commodityMap == null || commodityMap.isEmpty()) {
            return true;
        }

        // 1. 按 commodityId 排序，防止死锁
        List<Long> sortedIds = commodityMap.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

        // 2. 按顺序获取所有锁
        List<RLock> locks = new ArrayList<>();
        try {
            for (Long commodityId : sortedIds) {
                RLock lock = redissonClient.getLock(STOCK_LOCK_KEY + commodityId);
                // 尝试加锁，等待 3 秒，锁持有时间 30 秒（WatchDog 自动续期）
                boolean locked = lock.tryLock(3, 30, TimeUnit.SECONDS);
                if (!locked) {
                    log.warn("获取库存锁失败，commodityId: {}", commodityId);
                    return false;
                }
                locks.add(lock);
            }

            // 3. 检查库存并扣减
            for (Long commodityId : sortedIds) {
                int quantity = commodityMap.get(commodityId);
                Commodity commodity = commodityRepository.findById(commodityId)
                        .orElseThrow(() -> new RuntimeException("商品不存在: " + commodityId));

                if (commodity.getStock() == null || commodity.getStock() < quantity) {
                    String msg = String.format("商品 [%s] 库存不足: 当前 %d, 需求 %d",
                            commodity.getName(), commodity.getStock() == null ? 0 : commodity.getStock(), quantity);
                    log.warn(msg);
                    throw new RuntimeException(msg);
                }

                commodity.setStock(commodity.getStock() - quantity);
                commodityRepository.save(commodity);
                log.info("扣减库存成功，commodityId: {}, 扣减: {}, 剩余: {}",
                        commodityId, quantity, commodity.getStock());
            }

            return true;
        } catch (InterruptedException e) {
            log.error("库存扣减被中断", e);
            Thread.currentThread().interrupt();
            return false;
        } finally {
            // 4. 逆序释放锁
            for (int i = locks.size() - 1; i >= 0; i--) {
                try {
                    locks.get(i).unlock();
                } catch (Exception e) {
                    log.warn("释放库存锁异常", e);
                }
            }
        }
    }

    /**
     * 库存回退
     * @param commodityMap 商品ID -> 回退数量
     */
    @Override
    @Transactional
    public void restoreStock(Map<Long, Integer> commodityMap) {
        if (commodityMap == null || commodityMap.isEmpty()) {
            return;
        }

        List<Long> sortedIds = commodityMap.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

        List<RLock> locks = new ArrayList<>();
        try {
            for (Long commodityId : sortedIds) {
                RLock lock = redissonClient.getLock(STOCK_LOCK_KEY + commodityId);
                boolean locked = lock.tryLock(3, 30, TimeUnit.SECONDS);
                if (!locked) {
                    log.warn("获取库存锁失败（回退），commodityId: {}", commodityId);
                    return;
                }
                locks.add(lock);
            }

            for (Long commodityId : sortedIds) {
                int quantity = commodityMap.get(commodityId);
                commodityRepository.findById(commodityId).ifPresent(commodity -> {
                    commodity.setStock(commodity.getStock() + quantity);
                    commodityRepository.save(commodity);
                    log.info("回退库存成功，commodityId: {}, 回退: {}, 当前: {}",
                            commodityId, quantity, commodity.getStock());
                });
            }
        } catch (InterruptedException e) {
            log.error("库存回退被中断", e);
            Thread.currentThread().interrupt();
        } finally {
            for (int i = locks.size() - 1; i >= 0; i--) {
                try {
                    locks.get(i).unlock();
                } catch (Exception e) {
                    log.warn("释放库存锁异常", e);
                }
            }
        }
    }
}
