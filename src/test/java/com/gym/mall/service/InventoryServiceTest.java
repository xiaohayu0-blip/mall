package com.gym.mall.service;

import com.gym.mall.Repository.CommodityRepository;
import com.gym.mall.domain.entity.Commodity;
import com.gym.mall.service.Impl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.gym.mall.Constants.STOCK_LOCK_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("库存服务单元测试")
class InventoryServiceTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private CommodityRepository commodityRepository;

    @Mock
    private RLock rLock;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Commodity commodity;

    @BeforeEach
    void setUp() {
        commodity = Commodity.builder()
                .id(1L)
                .name("测试商品")
                .price(9900L)
                .stock(100)
                .status(1)
                .build();
    }

    // ===== deductStock =====

    @Test
    @DisplayName("扣减库存 - 正常扣减成功")
    void deductStock_success() throws InterruptedException {
        when(redissonClient.getLock(STOCK_LOCK_KEY + 1L)).thenReturn(rLock);
        when(rLock.tryLock(3, 30, TimeUnit.SECONDS)).thenReturn(true);
        when(commodityRepository.findById(1L)).thenReturn(Optional.of(commodity));
        when(commodityRepository.save(any())).thenReturn(commodity);

        boolean result = inventoryService.deductStock(Map.of(1L, 10));

        assertThat(result).isTrue();
        assertThat(commodity.getStock()).isEqualTo(90);
        verify(commodityRepository).save(commodity);
    }

    @Test
    @DisplayName("扣减库存 - 库存不足返回 false")
    void deductStock_insufficientStock_returnsFalse() throws InterruptedException {
        commodity.setStock(5);
        when(redissonClient.getLock(STOCK_LOCK_KEY + 1L)).thenReturn(rLock);
        when(rLock.tryLock(3, 30, TimeUnit.SECONDS)).thenReturn(true);
        when(commodityRepository.findById(1L)).thenReturn(Optional.of(commodity));

        boolean result = inventoryService.deductStock(Map.of(1L, 10));

        assertThat(result).isFalse();
        verify(commodityRepository, never()).save(any());
    }

    @Test
    @DisplayName("扣减库存 - 获取分布式锁失败返回 false")
    void deductStock_lockFailed_returnsFalse() throws InterruptedException {
        when(redissonClient.getLock(STOCK_LOCK_KEY + 1L)).thenReturn(rLock);
        when(rLock.tryLock(3, 30, TimeUnit.SECONDS)).thenReturn(false);

        boolean result = inventoryService.deductStock(Map.of(1L, 10));

        assertThat(result).isFalse();
        verify(commodityRepository, never()).findById(any());
    }

    @Test
    @DisplayName("扣减库存 - 商品不存在抛出异常")
    void deductStock_commodityNotFound_throwsException() throws InterruptedException {
        when(redissonClient.getLock(STOCK_LOCK_KEY + 1L)).thenReturn(rLock);
        when(rLock.tryLock(3, 30, TimeUnit.SECONDS)).thenReturn(true);
        when(commodityRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.deductStock(Map.of(1L, 10)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("商品不存在");
    }

    @Test
    @DisplayName("扣减库存 - 传入空 map 直接返回 true")
    void deductStock_emptyMap_returnsTrue() {
        boolean result = inventoryService.deductStock(Map.of());
        assertThat(result).isTrue();
        verifyNoInteractions(redissonClient, commodityRepository);
    }

    // ===== restoreStock =====

    @Test
    @DisplayName("回退库存 - 正常回退成功")
    void restoreStock_success() throws InterruptedException {
        when(redissonClient.getLock(STOCK_LOCK_KEY + 1L)).thenReturn(rLock);
        when(rLock.tryLock(3, 30, TimeUnit.SECONDS)).thenReturn(true);
        when(commodityRepository.findById(1L)).thenReturn(Optional.of(commodity));

        inventoryService.restoreStock(Map.of(1L, 10));

        assertThat(commodity.getStock()).isEqualTo(110);
        verify(commodityRepository).save(commodity);
    }

    @Test
    @DisplayName("回退库存 - 多商品按 ID 有序加锁（防死锁）")
    void restoreStock_multipleItems_lockedInOrder() throws InterruptedException {
        Commodity commodity2 = Commodity.builder().id(2L).stock(50).build();
        RLock lock2 = mock(RLock.class);

        when(redissonClient.getLock(STOCK_LOCK_KEY + 1L)).thenReturn(rLock);
        when(redissonClient.getLock(STOCK_LOCK_KEY + 2L)).thenReturn(lock2);
        when(rLock.tryLock(3, 30, TimeUnit.SECONDS)).thenReturn(true);
        when(lock2.tryLock(3, 30, TimeUnit.SECONDS)).thenReturn(true);
        when(commodityRepository.findById(1L)).thenReturn(Optional.of(commodity));
        when(commodityRepository.findById(2L)).thenReturn(Optional.of(commodity2));

        inventoryService.restoreStock(Map.of(1L, 5, 2L, 3));

        // 锁必须都被获取到
        verify(redissonClient).getLock(STOCK_LOCK_KEY + 1L);
        verify(redissonClient).getLock(STOCK_LOCK_KEY + 2L);
        // 库存均已回退
        assertThat(commodity.getStock()).isEqualTo(105);
        assertThat(commodity2.getStock()).isEqualTo(53);
    }
}
