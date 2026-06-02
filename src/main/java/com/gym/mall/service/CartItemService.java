package com.gym.mall.service;

import com.gym.mall.domain.entity.CartItem;

import java.util.List;

public interface CartItemService {

    List<CartItem> getItems(String sessionId);

    int getCartCount(String sessionId);

    void addToCart(String sessionId, Long commodityId, int quantity);

    void updateCartItem(String sessionId, Long commodityId, int quantity);

    void removeItem(String sessionId, Long commodityId);

    void clearCart(String sessionId);
}
