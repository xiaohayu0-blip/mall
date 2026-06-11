package com.gym.mall.controller;

import com.gym.mall.domain.dto.CartItemVO;
import com.gym.mall.domain.dto.OrderDTO;
import com.gym.mall.domain.dto.PageResponse;
import com.gym.mall.interceptor.AdminOnly;
import com.gym.mall.service.CartService;
import com.gym.mall.service.OrderService;
import com.gym.mall.utils.BaseContext;
import com.gym.mall.validator.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "订单管理", description = "订单创建、查询及状态流转")
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Operation(summary = "从购物车创建订单")
    @PostMapping("/create")
    public Response<OrderDTO> createOrder(
            @Parameter(description = "收货人姓名") @RequestParam String receiverName,
            @Parameter(description = "收货人电话") @RequestParam String receiverPhone,
            @Parameter(description = "收货地址") @RequestParam String receiverAddress,
            @Parameter(description = "备注") @RequestParam(required = false) String remark) {
        Long userId = BaseContext.getCurrentId();
        List<CartItemVO> selectedItems = cartService.getSelectedItems(userId);
        OrderDTO order = orderService.createOrder(userId, selectedItems,
                receiverName, receiverPhone, receiverAddress, remark);
        return Response.newSuccess(order);
    }

    @Operation(summary = "查询我的订单列表")
    @GetMapping("/my")
    public Response<PageResponse<OrderDTO>> getMyOrders(
            @Parameter(description = "页码") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @Parameter(description = "订单状态过滤") @RequestParam(required = false) String status) {
        Long userId = BaseContext.getCurrentId();
        return Response.newSuccess(orderService.getUserOrders(userId, page, pageSize, status));
    }

    @Operation(summary = "查询订单详情")
    @GetMapping("/{orderId}")
    public Response<OrderDTO> getOrderDetail(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        Long userId = BaseContext.getCurrentId();
        return Response.newSuccess(orderService.getOrderDetail(orderId, userId));
    }

    @Operation(summary = "取消订单")
    @PostMapping("/{orderId}/cancel")
    public Response<String> cancelOrder(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        Long userId = BaseContext.getCurrentId();
        orderService.cancelOrder(orderId, userId);
        return Response.newSuccess("订单已取消");
    }

    @Operation(summary = "确认收货")
    @PostMapping("/{orderId}/complete")
    public Response<String> completeOrder(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        Long userId = BaseContext.getCurrentId();
        orderService.completeOrder(orderId, userId);
        return Response.newSuccess("确认收货成功");
    }

    @Operation(summary = "管理端：查询所有订单")
    @GetMapping("/admin/list")
    public Response<PageResponse<OrderDTO>> adminGetOrders(
            @Parameter(description = "页码") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @Parameter(description = "订单状态过滤") @RequestParam(required = false) String status) {
        return Response.newSuccess(orderService.adminGetOrders(page, pageSize, status));
    }

    @Operation(summary = "管理端：发货", description = "管理员权限")
    @AdminOnly
    @PostMapping("/admin/{orderId}/ship")
    public Response<String> shipOrder(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        orderService.shipOrder(orderId);
        return Response.newSuccess("发货成功");
    }
}
