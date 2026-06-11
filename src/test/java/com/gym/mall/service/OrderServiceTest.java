package com.gym.mall.service;

import com.gym.mall.Repository.OrderItemRepository;
import com.gym.mall.Repository.OrderRepository;
import com.gym.mall.Repository.UserRepository;
import com.gym.mall.domain.dto.CartItemVO;
import com.gym.mall.domain.dto.OrderDTO;
import com.gym.mall.domain.entity.Order;
import com.gym.mall.domain.entity.OrderItem;
import com.gym.mall.service.Impl.OrderServiceImpl;
import com.gym.mall.utils.SnowflakeIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.gym.mall.service.OrderService.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("订单服务单元测试")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private CartService cartService;

    @Mock
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @InjectMocks
    private OrderServiceImpl orderService;

    private static final Long USER_ID = 1L;

    private List<CartItemVO> cartItems;
    private Order savedOrder;

    @BeforeEach
    void setUp() {
        cartItems = List.of(
                CartItemVO.builder()
                        .commodityId(10L)
                        .name("跑步鞋")
                        .price(29900L)   // 299.00 元，单位：分
                        .quantity(2)
                        .stock(100)
                        .build(),
                CartItemVO.builder()
                        .commodityId(20L)
                        .name("运动袜")
                        .price(1500L)    // 15.00 元，单位：分
                        .quantity(3)
                        .stock(200)
                        .build()
        );

        savedOrder = Order.builder()
                .id(999L)
                .orderNo("ORDER_NO_001")
                .userId(USER_ID)
                .totalAmount(64300L)    // 29900*2 + 1500*3 = 64300
                .status(STATUS_PENDING)
                .receiverName("张三")
                .receiverPhone("13800138000")
                .receiverAddress("北京市朝阳区")
                .build();
    }

    // ===== createOrder =====

    @Test
    @DisplayName("创建订单 - 正常流程：扣库存、保存订单、保存订单项、清空购物车")
    void createOrder_success() {
        when(snowflakeIdGenerator.nextId()).thenReturn(123456789L);
        when(inventoryService.deductStock(anyMap())).thenReturn(true);
        when(orderRepository.save(any())).thenReturn(savedOrder);
        when(orderItemRepository.saveAll(anyList())).thenReturn(List.of());

        OrderDTO result = orderService.createOrder(USER_ID, cartItems,
                "张三", "13800138000", "北京市朝阳区", null);

        // 验证返回结果
        assertThat(result).isNotNull();
        assertThat(result.getTotalAmount()).isEqualTo(64300L);
        assertThat(result.getStatus()).isEqualTo(STATUS_PENDING);

        // 验证库存扣减入参
        ArgumentCaptor<Map<Long, Integer>> stockMapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(inventoryService).deductStock(stockMapCaptor.capture());
        assertThat(stockMapCaptor.getValue()).containsEntry(10L, 2).containsEntry(20L, 3);

        // 验证订单项保存
        ArgumentCaptor<List<OrderItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderItemRepository).saveAll(itemsCaptor.capture());
        assertThat(itemsCaptor.getValue()).hasSize(2);

        // 验证购物车清空
        verify(cartService).clearCart(USER_ID);
    }

    @Test
    @DisplayName("创建订单 - 购物车为空抛出异常")
    void createOrder_emptyCart_throwsException() {
        assertThatThrownBy(() -> orderService.createOrder(USER_ID, List.of(),
                "张三", "13800138000", "北京", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("购物车为空");

        verifyNoInteractions(inventoryService, orderRepository);
    }

    @Test
    @DisplayName("创建订单 - 库存不足抛出异常，不应保存订单")
    void createOrder_stockInsufficient_throwsException() {
        when(snowflakeIdGenerator.nextId()).thenReturn(123L);
        when(inventoryService.deductStock(anyMap())).thenReturn(false);

        assertThatThrownBy(() -> orderService.createOrder(USER_ID, cartItems,
                "张三", "13800138000", "北京", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("库存不足");

        verify(orderRepository, never()).save(any());
        verify(cartService, never()).clearCart(any());
    }

    @Test
    @DisplayName("创建订单 - 保存订单时异常，回退库存")
    void createOrder_saveOrderFails_restoresStock() {
        when(snowflakeIdGenerator.nextId()).thenReturn(123L);
        when(inventoryService.deductStock(anyMap())).thenReturn(true);
        when(orderRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> orderService.createOrder(USER_ID, cartItems,
                "张三", "13800138000", "北京", null))
                .isInstanceOf(RuntimeException.class);

        verify(inventoryService).restoreStock(anyMap());
        verify(cartService, never()).clearCart(any());
    }

    @Test
    @DisplayName("创建订单 - 总金额计算正确（价格已是分）")
    void createOrder_totalAmountCalculatedCorrectly() {
        when(snowflakeIdGenerator.nextId()).thenReturn(1L);
        when(inventoryService.deductStock(anyMap())).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1L);
            o.setStatus(STATUS_PENDING);
            return o;
        });
        when(orderItemRepository.saveAll(anyList())).thenReturn(List.of());

        OrderDTO result = orderService.createOrder(USER_ID, cartItems,
                "张三", "13800138000", "北京", null);

        // 29900*2 + 1500*3 = 64300 分 = 643.00 元
        assertThat(result.getTotalAmount()).isEqualTo(64300L);
        assertThat(result.getTotalAmountYuan()).isEqualTo("643.00");
    }

    // ===== cancelOrder =====

    @Test
    @DisplayName("取消订单 - 待支付状态可以取消，并回退库存")
    void cancelOrder_pendingOrder_success() {
        Order order = Order.builder()
                .id(1L).orderNo("NO001").userId(USER_ID).status(STATUS_PENDING).build();
        List<OrderItem> items = List.of(
                OrderItem.builder().commodityId(10L).quantity(2).build()
        );
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(items);

        orderService.cancelOrder(1L, USER_ID);

        verify(inventoryService).restoreStock(Map.of(10L, 2));
        assertThat(order.getStatus()).isEqualTo(STATUS_CANCELLED);
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("取消订单 - 已支付状态不允许取消")
    void cancelOrder_paidOrder_throwsException() {
        Order order = Order.builder()
                .id(1L).userId(USER_ID).status(STATUS_PAID).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L, USER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("不允许取消");

        verify(inventoryService, never()).restoreStock(anyMap());
    }

    @Test
    @DisplayName("取消订单 - 非本人订单抛出权限异常")
    void cancelOrder_wrongUser_throwsException() {
        Order order = Order.builder()
                .id(1L).userId(999L).status(STATUS_PENDING).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L, USER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("无权");
    }

    // ===== payOrder =====

    @Test
    @DisplayName("支付订单 - 待支付状态正常支付")
    void payOrder_success() {
        Order order = Order.builder()
                .orderNo("NO001").status(STATUS_PENDING).build();
        when(orderRepository.findByOrderNo("NO001")).thenReturn(Optional.of(order));

        orderService.payOrder("NO001");

        assertThat(order.getStatus()).isEqualTo(STATUS_PAID);
        assertThat(order.getPaidTime()).isNotNull();
    }

    @Test
    @DisplayName("支付订单 - 重复支付抛出异常")
    void payOrder_alreadyPaid_throwsException() {
        Order order = Order.builder()
                .orderNo("NO001").status(STATUS_PAID).build();
        when(orderRepository.findByOrderNo("NO001")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.payOrder("NO001"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("不是待支付");
    }

    // ===== shipOrder =====

    @Test
    @DisplayName("发货 - 已支付订单正常发货")
    void shipOrder_success() {
        Order order = Order.builder().id(1L).status(STATUS_PAID).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.shipOrder(1L);

        assertThat(order.getStatus()).isEqualTo(STATUS_SHIPPED);
        assertThat(order.getShippedTime()).isNotNull();
    }

    // ===== completeOrder =====

    @Test
    @DisplayName("确认收货 - 已发货状态正常完成")
    void completeOrder_success() {
        Order order = Order.builder().id(1L).userId(USER_ID).status(STATUS_SHIPPED).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.completeOrder(1L, USER_ID);

        assertThat(order.getStatus()).isEqualTo(STATUS_COMPLETED);
        assertThat(order.getCompletedTime()).isNotNull();
    }

    @Test
    @DisplayName("确认收货 - 未发货状态抛出异常")
    void completeOrder_notShipped_throwsException() {
        Order order = Order.builder().id(1L).userId(USER_ID).status(STATUS_PAID).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.completeOrder(1L, USER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("不是已发货");
    }
}
