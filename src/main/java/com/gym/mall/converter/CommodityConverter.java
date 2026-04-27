package com.gym.mall.converter;

import com.gym.mall.dao.Commodity;
import com.gym.mall.dto.commodityDTO;

public class CommodityConverter {
    public static commodityDTO converterCommodity(Commodity commodity){
        commodityDTO commodityDTO = new commodityDTO();
        commodityDTO.setId(commodity.getId());
        commodityDTO.setName(commodity.getName());
        commodityDTO.setPrice(commodity.getPrice());
        return commodityDTO;
    }

    public static Commodity converterCommodity(commodityDTO commodityDTO){
        Commodity commodity = new Commodity();
        commodity.setId(commodityDTO.getId());
        commodity.setName(commodityDTO.getName());
        commodity.setPrice(commodityDTO.getPrice());
        return commodity;
    }
}
