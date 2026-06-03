package com.gym.mall.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Long id;
    private Long commodityId;
    private String commodityName;
    private Long commodityPrice;
    private Integer quantity;
    private Long subtotal;
}
