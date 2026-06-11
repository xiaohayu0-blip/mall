package com.gym.mall.service;

import com.gym.mall.Repository.CommodityRepository;
import com.gym.mall.domain.dto.CartItemVO;
import com.gym.mall.domain.entity.Commodity;
import com.gym.mall.service.Impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.gym.mall.Constants.CART_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("购物车服务单元测试")
class CartServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private CommodityRepository commodityRepository;

    @Mock
    private HashOperations<String, Object, Object> hashOps;

    @Mock
    private SetOperations<String, Object> setOps;

    @InjectMocks
    private CartServiceImpl cartService;

    private static final Long USER_ID = 100L;
    private static final Long COMMODITY_ID = 1L;
    private static final String CART_HASH_KEY = CART_KEY + USER_ID;

    private Commodity commodity;

    @BeforeEach
    void setUp() {
        commodity = Commodity.builder()
                .id(COMMODITY_ID)
                .name("运动水壶")
                .price(2990L)
                .stock(50)
                .status(1)
                .build();
        when(redisTemplate.opsForHash()).thenReturn(hashOps);
    }

    // ===== addItem =====

    @Test
    @DisplayName("添加商品到购物车 - 新商品直接写入")
    void addItem_newItem_putsQuantity() {
        when(commodityRepository.findById(COMMODITY_ID)).thenReturn(Optional.of(commodity));
        when(hashOps.get(CART_HASH_KEY, COMMODITY_ID.toString())).thenReturn(null);

        cartService.addItem(USER_ID, COMMODITY_ID, 2);

        verify(hashOps).put(CART_HASH_KEY, COMMODITY_ID.toString(), "2");
    }

    @Test
    @DisplayName("添加商品到购物车 - 已存在则累加数量")
    void addItem_existingItem_accumulatesQuantity() {
        when(commodityRepository.findById(COMMODITY_ID)).thenReturn(Optional.of(commodity));
        when(hashOps.get(CART_HASH_KEY, COMMODITY_ID.toString())).thenReturn("3");

        cartService.addItem(USER_ID, COMMODITY_ID, 2);

        verify(hashOps).put(CART_HASH_KEY, COMMODITY_ID.toString(), "5");
    }

    @Test
    @DisplayName("添加商品到购物车 - 商品不存在抛出异常")
    void addItem_commodityNotFound_throwsException() {
        when(commodityRepository.findById(COMMODITY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem(USER_ID, COMMODITY_ID, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("商品不存在");
    }

    // ===== updateItem =====

    @Test
    @DisplayName("更新购物车数量 - 数量大于 0 时更新")
    void updateItem_positiveQuantity_updatesHash() {
        cartService.updateItem(USER_ID, COMMODITY_ID, 5);

        verify(hashOps).put(CART_HASH_KEY, COMMODITY_ID.toString(), "5");
    }

    @Test
    @DisplayName("更新购物车数量 - 数量为 0 时删除该项")
    void updateItem_zeroQuantity_deletesItem() {
        cartService.updateItem(USER_ID, COMMODITY_ID, 0);

        verify(hashOps).delete(CART_HASH_KEY, COMMODITY_ID.toString());
    }

    @Test
    @DisplayName("更新购物车数量 - 数量为负数时删除该项")
    void updateItem_negativeQuantity_deletesItem() {
        cartService.updateItem(USER_ID, COMMODITY_ID, -1);

        verify(hashOps).delete(CART_HASH_KEY, COMMODITY_ID.toString());
    }

    // ===== removeItem =====

    @Test
    @DisplayName("删除购物车商品")
    void removeItem_deletesHashField() {
        cartService.removeItem(USER_ID, COMMODITY_ID);

        verify(hashOps).delete(CART_HASH_KEY, COMMODITY_ID.toString());
    }

    // ===== clearCart =====

    @Test
    @DisplayName("清空购物车 - 删除整个 Hash key")
    void clearCart_deletesKey() {
        cartService.clearCart(USER_ID);

        verify(redisTemplate).delete(CART_HASH_KEY);
    }

    // ===== getCartItems =====

    @Test
    @DisplayName("获取购物车列表 - 返回正确的 VO 列表")
    void getCartItems_returnsCorrectVOList() {
        when(hashOps.entries(CART_HASH_KEY)).thenReturn(Map.of(COMMODITY_ID.toString(), "2"));
        when(commodityRepository.findById(COMMODITY_ID)).thenReturn(Optional.of(commodity));

        List<CartItemVO> items = cartService.getCartItems(USER_ID);

        assertThat(items).hasSize(1);
        CartItemVO item = items.get(0);
        assertThat(item.getCommodityId()).isEqualTo(COMMODITY_ID);
        assertThat(item.getName()).isEqualTo("运动水壶");
        assertThat(item.getPrice()).isEqualTo(2990L);
        assertThat(item.getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("获取购物车列表 - 购物车为空返回空列表")
    void getCartItems_emptyCart_returnsEmptyList() {
        when(hashOps.entries(CART_HASH_KEY)).thenReturn(Map.of());

        List<CartItemVO> items = cartService.getCartItems(USER_ID);

        assertThat(items).isEmpty();
    }

    // ===== getCartCount =====

    @Test
    @DisplayName("获取购物车商品件数 - 累加所有商品数量")
    void getCartCount_returnsSum() {
        when(hashOps.size(CART_HASH_KEY)).thenReturn(2L);
        when(hashOps.entries(CART_HASH_KEY)).thenReturn(
                Map.of("1", "3", "2", "5")
        );

        int count = cartService.getCartCount(USER_ID);

        assertThat(count).isEqualTo(8);
    }
}
