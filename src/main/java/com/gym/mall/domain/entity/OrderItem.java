package com.gym.mall.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单项实体 — 记录订单中每个商品的快照信息
 *
 * 为什么需要快照（commodityName/commodityPrice）？
 * 商品信息可能后续会修改（改名、改价），但订单历史记录不能跟着变
 */
@Entity
@Table(name = "order_item")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 订单 ID */
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /** 商品 ID */
    @Column(name = "commodity_id", nullable = false)
    private Long commodityId;

    /** 商品名称（快照） */
    @Column(name = "commodity_name", nullable = false)
    private String commodityName;

    /** 商品单价（单位：分） */
    @Column(name = "commodity_price", nullable = false)
    private Long commodityPrice;

    /** 购买数量 */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /** 小计金额（单位：分） */
    @Column(name = "subtotal", nullable = false)
    private Long subtotal;
}
