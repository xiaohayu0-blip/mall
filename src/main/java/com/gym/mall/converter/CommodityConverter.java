package com.gym.mall.converter;

import com.gym.mall.domain.entity.Commodity;
import com.gym.mall.domain.dto.commodityDTO;

public class CommodityConverter {
    public static commodityDTO converterCommodity(Commodity commodity) {
        if (commodity == null) {
            return null;
        }
        return commodityDTO.builder()
                .id(commodity.getId())
                .name(commodity.getName())
                .price(commodity.getPrice())
                .categoryId(commodity.getCategoryId())
                .description(commodity.getDescription())
                .stock(commodity.getStock())
                .build();
    }

    public static Commodity converterCommodity(commodityDTO commodityDTO) {
        if (commodityDTO == null) {
            return null;
        }
        Commodity commodity = new Commodity();
        commodity.setId(commodityDTO.getId());
        commodity.setName(commodityDTO.getName());
        commodity.setPrice(commodityDTO.getPrice());
        commodity.setCategoryId(commodityDTO.getCategoryId());
        commodity.setDescription(commodityDTO.getDescription());
        commodity.setStock(commodityDTO.getStock() != null ? commodityDTO.getStock() : 0);
        return commodity;
    }
}