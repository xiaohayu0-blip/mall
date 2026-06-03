package com.gym.mall.service;

import com.gym.mall.domain.dto.CartItemVO;

import java.util.List;

public interface CartService {

    /**
     * 添加商品到购物车（如果已存在则增加数量）
     */
    void addItem(Long userId, Long commodityId, int quantity);

    /**
     * 更新购物车中某商品的数量（设为 0 则移除）
     */
    void updateItem(Long userId, Long commodityId, int quantity);

    /**
     * 删除购物车中的商品
     */
    void removeItem(Long userId, Long commodityId);

    /**
     * 获取购物车商品列表（含商品详情）
     */
    List<CartItemVO> getCartItems(Long userId);

    /**
     * 获取购物车商品总数
     */
    int getCartCount(Long userId);

    /**
     * 清空购物车
     */
    void clearCart(Long userId);

    /**
     * 选中/取消选中购物车中的商品
     */
    void toggleSelect(Long userId, Long commodityId, Boolean selected);

    /**
     * 获取选中的购物车商品（用于下单）
     */
    List<CartItemVO> getSelectedItems(Long userId);
}
