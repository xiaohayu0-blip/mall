package com.gym.mall.controller;

import com.gym.mall.domain.dto.CartItemVO;
import com.gym.mall.service.CartService;
import com.gym.mall.utils.BaseContext;
import com.gym.mall.validator.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 添加商品到购物车
     */
    @PostMapping("/add")
    public Response<String> addToCart(@RequestParam Long commodityId,
                                      @RequestParam(defaultValue = "1") int quantity) {
        Long userId = BaseContext.getCurrentId();
        cartService.addItem(userId, commodityId, quantity);
        return Response.newSuccess("添加成功");
    }

    /**
     * 更新购物车商品数量
     */
    @PutMapping("/update")
    public Response<String> updateCartItem(@RequestParam Long commodityId,
                                           @RequestParam int quantity) {
        Long userId = BaseContext.getCurrentId();
        cartService.updateItem(userId, commodityId, quantity);
        return Response.newSuccess("更新成功");
    }

    /**
     * 删除购物车中的商品
     */
    @DeleteMapping("/remove/{commodityId}")
    public Response<String> removeFromCart(@PathVariable Long commodityId) {
        Long userId = BaseContext.getCurrentId();
        cartService.removeItem(userId, commodityId);
        return Response.newSuccess("删除成功");
    }

    /**
     * 获取购物车列表
     */
    @GetMapping("/list")
    public Response<List<CartItemVO>> getCartList() {
        Long userId = BaseContext.getCurrentId();
        return Response.newSuccess(cartService.getCartItems(userId));
    }

    /**
     * 获取购物车商品数量
     */
    @GetMapping("/count")
    public Response<Integer> getCartCount() {
        Long userId = BaseContext.getCurrentId();
        return Response.newSuccess(cartService.getCartCount(userId));
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clear")
    public Response<String> clearCart() {
        Long userId = BaseContext.getCurrentId();
        cartService.clearCart(userId);
        return Response.newSuccess("购物车已清空");
    }

    /**
     * 获取选中的购物车商品（用于结算）
     */
    @GetMapping("/selected")
    public Response<List<CartItemVO>> getSelectedItems() {
        Long userId = BaseContext.getCurrentId();
        return Response.newSuccess(cartService.getSelectedItems(userId));
    }
}
