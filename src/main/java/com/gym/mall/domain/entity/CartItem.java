package com.gym.mall.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name="cart_item")
public class CartItem extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sessionId;

    @ManyToOne
    @JoinColumn(name = "commodity_id")
    private Commodity commodity;

    private Integer quantity;

    public CartItem(String sessionId, Commodity commodity, Integer quantity) {
        this.sessionId = sessionId;
        this.commodity = commodity;
        this.quantity = quantity;
    }
}
