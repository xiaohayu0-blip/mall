package com.gym.mall.controller;

import com.gym.mall.domain.dto.CartItemVO;
import com.gym.mall.service.CartService;
import com.gym.mall.utils.BaseContext;
import com.gym.mall.validator.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "购物车", description = "购物车商品管理")
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Operation(summary = "添加商品到购物车")
    @PostMapping("/add")
    public Response<String> addToCart(
            @Parameter(description = "商品ID") @RequestParam Long commodityId,
            @Parameter(description = "数量，默认1") @RequestParam(defaultValue = "1") int quantity) {
        Long userId = BaseContext.getCurrentId();
        cartService.addItem(userId, commodityId, quantity);
        return Response.newSuccess("添加成功");
    }

    @Operation(summary = "更新购物车商品数量")
    @PutMapping("/update")
    public Response<String> updateCartItem(
            @Parameter(description = "商品ID") @RequestParam Long commodityId,
            @Parameter(description = "新数量") @RequestParam int quantity) {
        Long userId = BaseContext.getCurrentId();
        cartService.updateItem(userId, commodityId, quantity);
        return Response.newSuccess("更新成功");
    }

    @Operation(summary = "删除购物车中的商品")
    @DeleteMapping("/remove/{commodityId}")
    public Response<String> removeFromCart(
            @Parameter(description = "商品ID") @PathVariable Long commodityId) {
        Long userId = BaseContext.getCurrentId();
        cartService.removeItem(userId, commodityId);
        return Response.newSuccess("删除成功");
    }

    @Operation(summary = "获取购物车商品列表")
    @GetMapping("/list")
    public Response<List<CartItemVO>> getCartList() {
        Long userId = BaseContext.getCurrentId();
        return Response.newSuccess(cartService.getCartItems(userId));
    }

    @Operation(summary = "获取购物车商品总数")
    @GetMapping("/count")
    public Response<Integer> getCartCount() {
        Long userId = BaseContext.getCurrentId();
        return Response.newSuccess(cartService.getCartCount(userId));
    }

    @Operation(summary = "清空购物车")
    @DeleteMapping("/clear")
    public Response<String> clearCart() {
        Long userId = BaseContext.getCurrentId();
        cartService.clearCart(userId);
        return Response.newSuccess("购物车已清空");
    }

    @Operation(summary = "获取已选中的购物车商品（用于结算）")
    @GetMapping("/selected")
    public Response<List<CartItemVO>> getSelectedItems() {
        Long userId = BaseContext.getCurrentId();
        return Response.newSuccess(cartService.getSelectedItems(userId));
    }

    @Operation(summary = "切换购物车商品选中状态")
    @PutMapping("/toggle-select")
    public Response<String> toggleSelect(
            @Parameter(description = "商品ID") @RequestParam Long commodityId,
            @Parameter(description = "是否选中") @RequestParam Boolean selected) {
        Long userId = BaseContext.getCurrentId();
        cartService.toggleSelect(userId, commodityId, selected);
        return Response.newSuccess("操作成功");
    }
}
