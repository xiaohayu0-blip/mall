package com.gym.mall.service.Impl;

import com.gym.mall.Repository.OrderItemRepository;
import com.gym.mall.Repository.OrderRepository;
import com.gym.mall.Repository.UserRepository;
import com.gym.mall.domain.dto.*;
import com.gym.mall.domain.entity.Order;
import com.gym.mall.domain.entity.OrderItem;
import com.gym.mall.domain.entity.User;
import com.gym.mall.service.CartService;
import com.gym.mall.service.InventoryService;
import com.gym.mall.service.OrderService;
import com.gym.mall.utils.SnowflakeIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单服务
 *
 * 核心流程：购物车选中 → 创建订单（锁库存）→ 支付 → 发货 → 完成
 * 异常流程：取消订单（回退库存）
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private CartService cartService;

    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    @Transactional
    public OrderDTO createOrder(Long userId, List<CartItemVO> items,
                                String name, String phone, String address, String remark) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("购物车为空，请选择商品后再下单");
        }

        // 1. 生成订单号（雪花算法）
        String orderNo = String.valueOf(snowflakeIdGenerator.nextId());

        // 2. 准备库存扣减映射
        Map<Long, Integer> stockMap = new HashMap<>();
        for (CartItemVO item : items) {
            stockMap.put(item.getCommodityId(), item.getQuantity());
        }

        // 3. 扣减库存（分布式锁）
        boolean stockResult = inventoryService.deductStock(stockMap);
        if (!stockResult) {
            throw new RuntimeException("系统繁忙，请稍后重试");
        }

        try {
            // 4. 计算总金额（price 已是分，直接累加）
            long totalAmount = 0;
            List<OrderItem> orderItems = new ArrayList<>();

            for (CartItemVO item : items) {
                long priceInCents = item.getPrice();
                long subtotal = priceInCents * item.getQuantity();
                totalAmount += subtotal;

                orderItems.add(OrderItem.builder()
                        .commodityId(item.getCommodityId())
                        .commodityName(item.getName())
                        .commodityPrice(priceInCents)
                        .quantity(item.getQuantity())
                        .subtotal(subtotal)
                        .build());
            }

            // 5. 保存订单
            Order order = Order.builder()
                    .orderNo(orderNo)
                    .userId(userId)
                    .totalAmount(totalAmount)
                    .status(STATUS_PENDING)
                    .receiverName(name)
                    .receiverPhone(phone)
                    .receiverAddress(address)
                    .remark(remark)
                    .build();

            order = orderRepository.save(order);

            // 6. 保存订单项
            Long finalOrderId = order.getId();
            orderItems.forEach(item -> item.setOrderId(finalOrderId));
            orderItemRepository.saveAll(orderItems);

            // 7. 清空购物车
            cartService.clearCart(userId);

            log.info("订单创建成功, orderNo: {}, userId: {}, totalAmount: {}分",
                    orderNo, userId, totalAmount);

            return toOrderDTO(order, orderItems, null);

        } catch (Exception e) {
            // 出现任何异常，回退库存
            log.error("订单创建失败，回退库存", e);
            inventoryService.restoreStock(stockMap);
            throw e;
        }
    }

    @Override
    public PageResponse<OrderDTO> getUserOrders(Long userId, Integer page, Integer pageSize, String status) {
        int pageNum = page != null && page > 0 ? page - 1 : 0;
        int size = pageSize != null && pageSize > 0 ? pageSize : 10;

        Pageable pageable = PageRequest.of(pageNum, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Order> orderPage;

        if (status != null && !status.isEmpty()) {
            orderPage = orderRepository.findByUserIdAndStatusOrderByCreateTimeDesc(userId, status, pageable);
        } else {
            orderPage = orderRepository.findByUserIdOrderByCreateTimeDesc(userId, pageable);
        }

        return buildPageResponse(orderPage);
    }

    @Override
    public OrderDTO getOrderDetail(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        // 校验权限：只能查看自己的订单（管理员除外）
        if (!order.getUserId().equals(userId)) {
            String role = com.gym.mall.utils.BaseContext.getCurrentRole();
            if (!"ADMIN".equals(role)) {
                throw new RuntimeException("无权查看该订单");
            }
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        User user = userRepository.findById(order.getUserId()).orElse(null);
        return toOrderDTO(order, items, user);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权取消该订单");
        }

        if (!STATUS_PENDING.equals(order.getStatus())) {
            throw new RuntimeException("当前订单状态不允许取消");
        }

        // 回退库存
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        Map<Long, Integer> stockMap = items.stream()
                .collect(Collectors.toMap(OrderItem::getCommodityId, OrderItem::getQuantity));
        inventoryService.restoreStock(stockMap);

        order.setStatus(STATUS_CANCELLED);
        order.setCancelledTime(Instant.now().toEpochMilli());
        orderRepository.save(order);
        log.info("订单已取消, orderNo: {}", order.getOrderNo());
    }

    @Override
    @Transactional
    public void payOrder(String orderNo) {
        long paidTime = Instant.now().toEpochMilli();
        // 原子更新：只在订单状态为 PENDING 时更新为 PAID
        int affected = orderRepository.updateStatusToPaid(orderNo, paidTime);
        if (affected == 0) {
            // 可能是订单不存在，或状态不是 PENDING（已支付/已取消）
            Order order = orderRepository.findByOrderNo(orderNo)
                    .orElseThrow(() -> new RuntimeException("订单不存在"));
            throw new RuntimeException("订单状态不是待支付，无法支付");
        }
        log.info("订单支付成功, orderNo: {}", orderNo);
    }

    @Override
    @Transactional
    public void shipOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!STATUS_PAID.equals(order.getStatus())) {
            throw new RuntimeException("订单状态不是已支付，无法发货");
        }

        order.setStatus(STATUS_SHIPPED);
        order.setShippedTime(Instant.now().toEpochMilli());
        orderRepository.save(order);
        log.info("订单已发货, orderNo: {}", order.getOrderNo());
    }

    @Override
    @Transactional
    public void completeOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该订单");
        }

        if (!STATUS_SHIPPED.equals(order.getStatus())) {
            throw new RuntimeException("订单状态不是已发货，无法确认收货");
        }

        order.setStatus(STATUS_COMPLETED);
        order.setCompletedTime(Instant.now().toEpochMilli());
        orderRepository.save(order);
        log.info("订单已完成, orderNo: {}", order.getOrderNo());
    }

    @Override
    public PageResponse<OrderDTO> adminGetOrders(Integer page, Integer pageSize, String status) {
        int pageNum = page != null && page > 0 ? page - 1 : 0;
        int size = pageSize != null && pageSize > 0 ? pageSize : 10;

        Pageable pageable = PageRequest.of(pageNum, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Order> orderPage;

        if (status != null && !status.isEmpty()) {
            orderPage = orderRepository.findByStatusOrderByCreateTimeDesc(status, pageable);
        } else {
            orderPage = orderRepository.findAllByOrderByCreateTimeDesc(pageable);
        }

        return buildPageResponse(orderPage);
    }

    // ========== 辅助方法 ==========

    /**
     * 将分转为元的字符串
     * 9999 -> "99.99"
     */
    private String centsToYuan(long cents) {
        BigDecimal yuan = new BigDecimal(cents).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP);
        return yuan.toString();
    }

    /**
     * 获取状态中文描述
     */
    private String getStatusDesc(String status) {
        return switch (status) {
            case STATUS_PENDING -> "待支付";
            case STATUS_PAID -> "已支付";
            case STATUS_SHIPPED -> "已发货";
            case STATUS_COMPLETED -> "已完成";
            case STATUS_CANCELLED -> "已取消";
            default -> status;
        };
    }

    private OrderDTO toOrderDTO(Order order, List<OrderItem> items, User user) {
        OrderDTO.OrderDTOBuilder builder = OrderDTO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .totalAmountYuan(centsToYuan(order.getTotalAmount()))
                .status(order.getStatus())
                .statusDesc(getStatusDesc(order.getStatus()))
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .receiverAddress(order.getReceiverAddress())
                .remark(order.getRemark())
                .paidTime(order.getPaidTime())
                .shippedTime(order.getShippedTime())
                .completedTime(order.getCompletedTime())
                .cancelledTime(order.getCancelledTime())
                .createTime(order.getCreateTime());

        if (user != null) {
            builder.userName(user.getUserName());
        }

        if (items != null && !items.isEmpty()) {
            builder.items(items.stream().map(item -> OrderItemDTO.builder()
                    .id(item.getId())
                    .commodityId(item.getCommodityId())
                    .commodityName(item.getCommodityName())
                    .commodityPrice(item.getCommodityPrice())
                    .quantity(item.getQuantity())
                    .subtotal(item.getSubtotal())
                    .build()).collect(Collectors.toList()));
        }

        return builder.build();
    }

    private PageResponse<OrderDTO> buildPageResponse(Page<Order> orderPage) {
        List<Order> orders = orderPage.getContent();

        // 批量查询用户信息
        Set<Long> userIds = orders.stream().map(Order::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, u -> u, (a, b) -> a));

        // 批量查询订单项
        List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
        Map<Long, List<OrderItem>> itemsMap = orderItemRepository.findByOrderIdIn(orderIds)
                .stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));

        List<OrderDTO> dtoList = orders.stream()
                .map(order -> toOrderDTO(
                        order,
                        itemsMap.getOrDefault(order.getId(), new ArrayList<>()),
                        userMap.get(order.getUserId())))
                .collect(Collectors.toList());

        return PageResponse.<OrderDTO>builder()
                .records(dtoList)
                .total(orderPage.getTotalElements())
                .page(orderPage.getNumber() + 1)
                .pageSize(orderPage.getSize())
                .totalPages(orderPage.getTotalPages())
                .build();
    }
}
