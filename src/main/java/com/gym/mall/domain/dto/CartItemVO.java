package com.gym.mall.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemVO {
    private Long commodityId;
    private String name;
    private String price;
    private String image;
    private Integer quantity;
    private Integer stock;
    private Boolean selected;
}
