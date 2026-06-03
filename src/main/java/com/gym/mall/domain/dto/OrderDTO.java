package com.gym.mall.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 订单 DTO — 用于接口响应
 *
 * 金额单位：分（避免浮点数精度问题）
 * 前端展示时除以 100 转为元
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private String orderNo;
    private Long userId;
    private String userName;
    private Long totalAmount;
    private String totalAmountYuan; // 展示用（元）
    private String status;
    private String statusDesc;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String remark;
    private Long paidTime;
    private Long shippedTime;
    private Long completedTime;
    private Long cancelledTime;
    private Long createTime;
    private List<OrderItemDTO> items;
}
