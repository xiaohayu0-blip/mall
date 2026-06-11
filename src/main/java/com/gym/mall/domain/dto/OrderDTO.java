package com.gym.mall.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "订单信息（金额单位：分）")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "订单总金额（分）")
    private Long totalAmount;

    @Schema(description = "订单总金额（元，展示用）")
    private String totalAmountYuan;

    @Schema(description = "订单状态枚举值")
    private String status;

    @Schema(description = "订单状态描述")
    private String statusDesc;

    @Schema(description = "收货人姓名")
    private String receiverName;

    @Schema(description = "收货人电话")
    private String receiverPhone;

    @Schema(description = "收货地址")
    private String receiverAddress;

    @Schema(description = "买家备注")
    private String remark;

    @Schema(description = "支付时间（毫秒时间戳）")
    private Long paidTime;

    @Schema(description = "发货时间（毫秒时间戳）")
    private Long shippedTime;

    @Schema(description = "完成时间（毫秒时间戳）")
    private Long completedTime;

    @Schema(description = "取消时间（毫秒时间戳）")
    private Long cancelledTime;

    @Schema(description = "创建时间（毫秒时间戳）")
    private Long createTime;

    @Schema(description = "订单商品列表")
    private List<OrderItemDTO> items;
}
