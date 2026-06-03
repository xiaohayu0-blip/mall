package com.gym.mall.service.Impl;

import com.gym.mall.Repository.CommodityRepository;
import com.gym.mall.domain.dto.CartItemVO;
import com.gym.mall.domain.entity.Commodity;
import com.gym.mall.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.gym.mall.Constants.CART_KEY;

/**
 * 购物车服务 — Redis Hash 实现
 *
 * 数据结构：cart:{userId} → Hash(commodityId -> quantity)
 * 知识点：Redis Hash、缓存与 DB 一致性、过期策略
 *
 * 优势：
 * 1. O(1) 单字段读写，性能远高于 MySQL
 * 2. 购物车天然适合用 Hash 表达
 * 3. 可设置 TTL 自动清理僵尸购物车
 * 4. 贴近真实电商架构（京东/淘宝购物车核心在 Redis）
 */
@Service
@Slf4j
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CommodityRepository commodityRepository;

    private String cartKey(Long userId) {
        return CART_KEY + userId;
    }

    @Override
    public void addItem(Long userId, Long commodityId, int quantity) {
        String key = cartKey(userId);
        // 检查商品是否存在
        commodityRepository.findById(commodityId)
                .orElseThrow(() -> new RuntimeException("商品不存在: " + commodityId));

        // Redis Hash 操作：如果 field 已存在则返回旧值，否则新增
        Object existed = redisTemplate.opsForHash().get(key, commodityId.toString());
        if (existed != null) {
            int oldQty = Integer.parseInt(existed.toString());
            redisTemplate.opsForHash().put(key, commodityId.toString(), String.valueOf(oldQty + quantity));
        } else {
            redisTemplate.opsForHash().put(key, commodityId.toString(), String.valueOf(quantity));
        }
        log.info("购物车添加商品, userId:{}, commodityId:{}, quantity:{}", userId, commodityId, quantity);
    }

    @Override
    public void updateItem(Long userId, Long commodityId, int quantity) {
        String key = cartKey(userId);
        if (quantity <= 0) {
            redisTemplate.opsForHash().delete(key, commodityId.toString());
            log.info("购物车移除商品, userId:{}, commodityId:{}", userId, commodityId);
        } else {
            redisTemplate.opsForHash().put(key, commodityId.toString(), String.valueOf(quantity));
            log.info("购物车更新商品数量, userId:{}, commodityId:{}, quantity:{}", userId, commodityId, quantity);
        }
    }

    @Override
    public void removeItem(Long userId, Long commodityId) {
        String key = cartKey(userId);
        redisTemplate.opsForHash().delete(key, commodityId.toString());
        log.info("购物车删除商品, userId:{}, commodityId:{}", userId, commodityId);
    }

    @Override
    public List<CartItemVO> getCartItems(Long userId) {
        String key = cartKey(userId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

        if (entries.isEmpty()) {
            return new ArrayList<>();
        }

        List<CartItemVO> items = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            Long commodityId = Long.valueOf(entry.getKey().toString());
            int quantity = Integer.parseInt(entry.getValue().toString());

            commodityRepository.findById(commodityId).ifPresent(commodity -> {
                items.add(CartItemVO.builder()
                        .commodityId(commodityId)
                        .name(commodity.getName())
                        .price(commodity.getPrice())
                        .quantity(quantity)
                        .stock(commodity.getStock())
                        .selected(true)
                        .build());
            });
        }
        return items;
    }

    @Override
    public int getCartCount(Long userId) {
        String key = cartKey(userId);
        Long size = redisTemplate.opsForHash().size(key);
        int count = 0;
        if (size != null) {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            for (Object val : entries.values()) {
                count += Integer.parseInt(val.toString());
            }
        }
        return count;
    }

    @Override
    public void clearCart(Long userId) {
        String key = cartKey(userId);
        redisTemplate.delete(key);
        log.info("购物车清空, userId:{}", userId);
    }

    @Override
    public void toggleSelect(Long userId, Long commodityId, Boolean selected) {
        // 选中状态用 Redis Hash 的额外字段存储，格式 commodityId -> "qty:selected"
        // 简化实现：直接使用一个独立的 Set 存储选中的商品 ID
        String selectKey = CART_KEY + userId + ":selected";
        if (Boolean.TRUE.equals(selected)) {
            redisTemplate.opsForSet().add(selectKey, commodityId.toString());
        } else {
            redisTemplate.opsForSet().remove(selectKey, commodityId.toString());
        }
    }

    @Override
    public List<CartItemVO> getSelectedItems(Long userId) {
        String key = cartKey(userId);
        String selectKey = CART_KEY + userId + ":selected";

        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        // 获取选中的商品 ID
        List<CartItemVO> items = new ArrayList<>();

        if (entries.isEmpty()) {
            return items;
        }

        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            Long commodityId = Long.valueOf(entry.getKey().toString());
            int quantity = Integer.parseInt(entry.getValue().toString());

            Commodity commodity = commodityRepository.findById(commodityId).orElse(null);
            if (commodity == null) continue;

            items.add(CartItemVO.builder()
                    .commodityId(commodityId)
                    .name(commodity.getName())
                    .price(commodity.getPrice())
                    .quantity(quantity)
                    .stock(commodity.getStock())
                    .selected(true)
                    .build());
        }
        return items;
    }
}
