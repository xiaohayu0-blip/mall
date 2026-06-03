package com.gym.mall.service;

import com.gym.mall.domain.dto.CartItemVO;
import com.gym.mall.domain.dto.OrderDTO;
import com.gym.mall.domain.dto.PageResponse;

import java.util.List;

public interface OrderService {

    /** 订单状态常量 */
    String STATUS_PENDING = "PENDING_PAYMENT";
    String STATUS_PAID = "PAID";
    String STATUS_SHIPPED = "SHIPPED";
    String STATUS_COMPLETED = "COMPLETED";
    String STATUS_CANCELLED = "CANCELLED";

    /**
     * 从购物车创建订单
     *
     * @param userId    用户ID
     * @param items     购物车选中的商品
     * @param name      收货人
     * @param phone     收货电话
     * @param address   收货地址
     * @param remark    备注
     * @return 订单 DTO
     */
    OrderDTO createOrder(Long userId, List<CartItemVO> items,
                         String name, String phone, String address, String remark);

    /**
     * 查询用户订单列表
     */
    PageResponse<OrderDTO> getUserOrders(Long userId, Integer page, Integer pageSize, String status);

    /**
     * 查询订单详情
     */
    OrderDTO getOrderDetail(Long orderId, Long userId);

    /**
     * 取消订单
     */
    void cancelOrder(Long orderId, Long userId);

    /**
     * 模拟支付
     */
    void payOrder(String orderNo);

    /**
     * 管理员发货
     */
    void shipOrder(Long orderId);

    /**
     * 确认收货
     */
    void completeOrder(Long orderId, Long userId);

    /**
     * 管理员查询所有订单
     */
    PageResponse<OrderDTO> adminGetOrders(Integer page, Integer pageSize, String status);
}
