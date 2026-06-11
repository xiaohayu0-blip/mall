package com.gym.mall.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "购物车商品条目")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemVO {
    @Schema(description = "商品ID")
    private Long commodityId;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "单价（单位：分）")
    private Long price;

    @Schema(description = "商品图片URL")
    private String image;

    @Schema(description = "购物车中的数量")
    private Integer quantity;

    @Schema(description = "当前库存")
    private Integer stock;

    @Schema(description = "是否选中（用于结算）")
    private Boolean selected;
}
