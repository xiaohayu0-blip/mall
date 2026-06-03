package com.gym.mall.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单实体
 *
 * 订单状态流转：
 * PENDING_PAYMENT（待支付）→ PAID（已支付）→ SHIPPED（已发货）→ COMPLETED（已完成）
 *                      ↘ CANCELLED（已取消）
 */
@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 订单号（雪花算法生成） */
    @Column(name = "order_no", nullable = false, unique = true)
    private String orderNo;

    /** 用户 ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 订单总金额（单位：分，避免浮点数精度问题） */
    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    /** 订单状态 */
    @Column(name = "status", nullable = false)
    private String status;

    /** 收货人姓名 */
    @Column(name = "receiver_name")
    private String receiverName;

    /** 收货人电话 */
    @Column(name = "receiver_phone")
    private String receiverPhone;

    /** 收货地址 */
    @Column(name = "receiver_address")
    private String receiverAddress;

    /** 订单备注 */
    @Column(name = "remark")
    private String remark;

    /** 支付时间 */
    @Column(name = "paid_time")
    private Long paidTime;

    /** 发货时间 */
    @Column(name = "shipped_time")
    private Long shippedTime;

    /** 完成时间 */
    @Column(name = "completed_time")
    private Long completedTime;

    /** 取消时间 */
    @Column(name = "cancelled_time")
    private Long cancelledTime;
}
