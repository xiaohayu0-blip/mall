package com.gym.mall.integration;

import com.gym.mall.Repository.CommodityRepository;
import com.gym.mall.Repository.OrderItemRepository;
import com.gym.mall.Repository.OrderRepository;
import com.gym.mall.Repository.UserRepository;
import com.gym.mall.domain.dto.CartItemVO;
import com.gym.mall.domain.dto.OrderDTO;
import com.gym.mall.domain.entity.Commodity;
import com.gym.mall.domain.entity.Order;
import com.gym.mall.domain.entity.OrderItem;
import com.gym.mall.domain.entity.User;
import com.gym.mall.service.CartService;
import com.gym.mall.service.CommoditySearchService;
import com.gym.mall.service.InventoryService;
import com.gym.mall.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.amqp.autoconfigure.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.boot.data.redis.autoconfigure.DataRedisReactiveAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.gym.mall.service.OrderService.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 订单创建集成测试
 *
 * 策略：
 * - 数据库：H2 内存库（application-test.properties），JPA 自动建表
 * - Redis / Redisson / RabbitMQ：@MockitoBean 替换，不依赖外部服务
 * - InventoryService：使用真实实现，但 RedissonClient 被 Mock
 * - 每个测试方法在事务中运行，结束后自动回滚，保证测试隔离
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("订单创建流程集成测试")
@EnableAutoConfiguration(exclude = {
        RabbitAutoConfiguration.class,
        DataRedisAutoConfiguration.class,
        DataRedisReactiveAutoConfiguration.class
})
class OrderCreateIntegrationTest {

    // ===== 需要 Mock 的外部依赖 =====

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
    private CartService cartService;

    @MockitoBean
    private CommoditySearchService commoditySearchService;

    // ===== 真实 Bean（H2 数据库）=====

    @Autowired
    private OrderService orderService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CommodityRepository commodityRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Commodity shoe;
    private Commodity sock;

    @BeforeEach
    void setUp() {
        // 插入测试用户
        testUser = userRepository.save(User.builder()
                .userName("test_user")
                .password("hashed_password")
                .role("USER")
                .salt("salt123")
                .build());

        // 插入测试商品
        shoe = commodityRepository.save(Commodity.builder()
                .name("跑步鞋")
                .price(29900L)
                .categoryId(1L)
                .stock(100)
                .status(1)
                .build());

        sock = commodityRepository.save(Commodity.builder()
                .name("运动袜")
                .price(1500L)
                .categoryId(1L)
                .stock(200)
                .status(1)
                .build());

        // Mock Redisson 分布式锁（集成测试不需要真实 Redis 锁）
        var mockLock = mock(org.redisson.api.RLock.class);
        when(redissonClient.getLock(anyString())).thenReturn(mockLock);
        try {
            when(mockLock.tryLock(anyLong(), anyLong(), any())).thenReturn(true);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("完整下单流程：创建订单后，库存扣减、订单持久化、订单项持久化均正确")
    void createOrder_fullFlow_allPersisted() {
        List<CartItemVO> items = List.of(
                CartItemVO.builder()
                        .commodityId(shoe.getId())
                        .name(shoe.getName())
                        .price(shoe.getPrice())
                        .quantity(2)
                        .stock(shoe.getStock())
                        .build(),
                CartItemVO.builder()
                        .commodityId(sock.getId())
                        .name(sock.getName())
                        .price(sock.getPrice())
                        .quantity(3)
                        .stock(sock.getStock())
                        .build()
        );

        OrderDTO result = orderService.createOrder(
                testUser.getUser_id(), items,
                "张三", "13800138000", "北京市朝阳区", "尽快发货");

        // 1. 返回 DTO 正确
        assertThat(result.getOrderNo()).isNotBlank();
        assertThat(result.getStatus()).isEqualTo(STATUS_PENDING);
        assertThat(result.getTotalAmount()).isEqualTo(29900L * 2 + 1500L * 3); // 64300
        assertThat(result.getTotalAmountYuan()).isEqualTo("643.00");
        assertThat(result.getItems()).hasSize(2);

        // 2. 订单持久化到 H2
        Order saved = orderRepository.findByOrderNo(result.getOrderNo()).orElseThrow();
        assertThat(saved.getUserId()).isEqualTo(testUser.getUser_id());
        assertThat(saved.getReceiverName()).isEqualTo("张三");
        assertThat(saved.getRemark()).isEqualTo("尽快发货");

        // 3. 订单项持久化到 H2
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(saved.getId());
        assertThat(orderItems).hasSize(2);

        OrderItem shoeItem = orderItems.stream()
                .filter(i -> i.getCommodityId().equals(shoe.getId()))
                .findFirst().orElseThrow();
        assertThat(shoeItem.getCommodityName()).isEqualTo("跑步鞋");
        assertThat(shoeItem.getCommodityPrice()).isEqualTo(29900L);
        assertThat(shoeItem.getQuantity()).isEqualTo(2);
        assertThat(shoeItem.getSubtotal()).isEqualTo(59800L);

        // 4. 库存已扣减（H2 中查询）
        Commodity updatedShoe = commodityRepository.findById(shoe.getId()).orElseThrow();
        assertThat(updatedShoe.getStock()).isEqualTo(98); // 100 - 2

        Commodity updatedSock = commodityRepository.findById(sock.getId()).orElseThrow();
        assertThat(updatedSock.getStock()).isEqualTo(197); // 200 - 3

        // 5. 清空购物车
        verify(cartService).clearCart(testUser.getUser_id());
    }

    @Test
    @DisplayName("购物车为空时，拒绝创建订单")
    void createOrder_emptyCart_rejected() {
        assertThatThrownBy(() -> orderService.createOrder(
                testUser.getUser_id(), List.of(),
                "张三", "13800138000", "北京", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("购物车为空");

        assertThat(orderRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("库存不足时，订单不创建，库存不变")
    void createOrder_stockInsufficient_noOrderSaved() {
        // sock 库存只有 200，但请求 300
        List<CartItemVO> items = List.of(
                CartItemVO.builder()
                        .commodityId(sock.getId())
                        .name(sock.getName())
                        .price(sock.getPrice())
                        .quantity(300)
                        .stock(sock.getStock())
                        .build()
        );

        assertThatThrownBy(() -> orderService.createOrder(
                testUser.getUser_id(), items,
                "张三", "13800138000", "北京", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("库存不足");

        // 订单未创建
        assertThat(orderRepository.findAll()).isEmpty();

        // 库存未变
        Commodity unchanged = commodityRepository.findById(sock.getId()).orElseThrow();
        assertThat(unchanged.getStock()).isEqualTo(200);
    }

    @Test
    @DisplayName("完整状态流转：PENDING -> PAID -> SHIPPED -> COMPLETED")
    void orderLifecycle_fullStateTransition() {
        List<CartItemVO> items = List.of(
                CartItemVO.builder()
                        .commodityId(shoe.getId())
                        .name(shoe.getName())
                        .price(shoe.getPrice())
                        .quantity(1)
                        .stock(shoe.getStock())
                        .build()
        );

        OrderDTO created = orderService.createOrder(
                testUser.getUser_id(), items, "李四", "13900139000", "上海", null);
        String orderNo = created.getOrderNo();
        Long orderId = created.getId();

        // PENDING -> PAID
        orderService.payOrder(orderNo);
        assertThat(orderRepository.findByOrderNo(orderNo).orElseThrow().getStatus())
                .isEqualTo(STATUS_PAID);

        // PAID -> SHIPPED
        orderService.shipOrder(orderId);
        assertThat(orderRepository.findById(orderId).orElseThrow().getStatus())
                .isEqualTo(STATUS_SHIPPED);

        // SHIPPED -> COMPLETED
        orderService.completeOrder(orderId, testUser.getUser_id());
        Order completed = orderRepository.findById(orderId).orElseThrow();
        assertThat(completed.getStatus()).isEqualTo(STATUS_COMPLETED);
        assertThat(completed.getCompletedTime()).isNotNull().isPositive();
    }

    @Test
    @DisplayName("取消订单后，库存回退到原始值")
    void cancelOrder_stockRestored() {
        List<CartItemVO> items = List.of(
                CartItemVO.builder()
                        .commodityId(shoe.getId())
                        .name(shoe.getName())
                        .price(shoe.getPrice())
                        .quantity(5)
                        .stock(shoe.getStock())
                        .build()
        );

        OrderDTO created = orderService.createOrder(
                testUser.getUser_id(), items, "王五", "13700137000", "广州", null);

        // 下单后库存应已扣减
        assertThat(commodityRepository.findById(shoe.getId()).orElseThrow().getStock())
                .isEqualTo(95);

        // 取消订单
        orderService.cancelOrder(created.getId(), testUser.getUser_id());

        // 库存回退
        assertThat(commodityRepository.findById(shoe.getId()).orElseThrow().getStock())
                .isEqualTo(100);

        // 订单状态变为已取消
        assertThat(orderRepository.findById(created.getId()).orElseThrow().getStatus())
                .isEqualTo(STATUS_CANCELLED);
    }
}
