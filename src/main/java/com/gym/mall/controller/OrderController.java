package com.gym.mall.controller;

import com.gym.mall.domain.dto.CartItemVO;
import com.gym.mall.domain.dto.OrderDTO;
import com.gym.mall.domain.dto.PageResponse;
import com.gym.mall.interceptor.AdminOnly;
import com.gym.mall.service.CartService;
import com.gym.mall.service.OrderService;
import com.gym.mall.utils.BaseContext;
import com.gym.mall.validator.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    /**
     * 从购物车创建订单
     */
    @PostMapping("/create")
    public Response<OrderDTO> createOrder(
            @RequestParam String receiverName,
            @RequestParam String receiverPhone,
            @RequestParam String receiverAddress,
            @RequestParam(required = false) String remark) {
        Long userId = BaseContext.getCurrentId();
        List<CartItemVO> selectedItems = cartService.getSelectedItems(userId);
        OrderDTO order = orderService.createOrder(userId, selectedItems,
                receiverName, receiverPhone, receiverAddress, remark);
        return Response.newSuccess(order);
    }

    /**
     * 查询我的订单列表
     */
    @GetMapping("/my")
    public Response<PageResponse<OrderDTO>> getMyOrders(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        Long userId = BaseContext.getCurrentId();
        return Response.newSuccess(orderService.getUserOrders(userId, page, pageSize, status));
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/{orderId}")
    public Response<OrderDTO> getOrderDetail(@PathVariable Long orderId) {
        Long userId = BaseContext.getCurrentId();
        return Response.newSuccess(orderService.getOrderDetail(orderId, userId));
    }

    /**
     * 取消订单
     */
    @PostMapping("/{orderId}/cancel")
    public Response<String> cancelOrder(@PathVariable Long orderId) {
        Long userId = BaseContext.getCurrentId();
        orderService.cancelOrder(orderId, userId);
        return Response.newSuccess("订单已取消");
    }

    /**
     * 确认收货
     */
    @PostMapping("/{orderId}/complete")
    public Response<String> completeOrder(@PathVariable Long orderId) {
        Long userId = BaseContext.getCurrentId();
        orderService.completeOrder(orderId, userId);
        return Response.newSuccess("确认收货成功");
    }

    // ===== 管理端接口 =====

    /**
     * 管理端：查询所有订单
     */
    @GetMapping("/admin/list")
    public Response<PageResponse<OrderDTO>> adminGetOrders(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        return Response.newSuccess(orderService.adminGetOrders(page, pageSize, status));
    }

    /**
     * 管理端：发货
     */
    @AdminOnly
    @PostMapping("/admin/{orderId}/ship")
    public Response<String> shipOrder(@PathVariable Long orderId) {
        orderService.shipOrder(orderId);
        return Response.newSuccess("发货成功");
    }
}
