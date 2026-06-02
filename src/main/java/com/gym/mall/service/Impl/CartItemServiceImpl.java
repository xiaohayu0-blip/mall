package com.gym.mall.service.Impl;

import com.gym.mall.Repository.CartItemRepository;
import com.gym.mall.Repository.CommodityRepository;
import com.gym.mall.domain.dto.commodityDTO;
import com.gym.mall.domain.entity.CartItem;
import com.gym.mall.domain.entity.Commodity;
import com.gym.mall.service.CartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartItemServiceImpl implements CartItemService {
    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CommodityRepository commodityRepository;

    @Autowired
    private CommodityServiceImpl commodityService;

    @Override
    public List<CartItem> getItems(String sessionId) {
        return cartItemRepository.findBySessionId(sessionId);
    }

    @Override
    public int getCartCount(String sessionId) {
        return cartItemRepository.findBySessionId(sessionId)
                .stream().mapToInt(CartItem::getQuantity).sum();
    }

    @Override
    public void addToCart(String sessionId, Long commodityId, int quantity) {
        Commodity commodity = commodityRepository.findById(commodityId)
                .orElseThrow(() -> new RuntimeException("商品不存在: " + commodityId));

        Optional<CartItem> existing = cartItemRepository.findBySessionIdAndCommodityId(sessionId, commodityId);
        if (existing.isPresent()) {
            CartItem cartItem = existing.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItemRepository.save(cartItem);
        } else {
            cartItemRepository.save(new CartItem(sessionId, commodity, quantity));
        }
    }

    @Override
    public void updateCartItem(String sessionId, Long commodityId, int quantity) {
        cartItemRepository.findBySessionIdAndCommodityId(sessionId, commodityId).ifPresent(item -> {
            if (quantity <= 0) {
                cartItemRepository.delete(item);
            } else {
                item.setQuantity(quantity);
                cartItemRepository.save(item);
            }
        });
    }

    @Override
    public void removeItem(String sessionId, Long commodityId) {
        cartItemRepository.findBySessionIdAndCommodityId(sessionId, commodityId).ifPresent(item -> {
            cartItemRepository.delete(item);
        });
    }

    @Override
    public void clearCart(String sessionId) {
        cartItemRepository.deleteBySessionId(sessionId);
    }
}
