package com.gym.mall.service;

import com.gym.mall.dto.commodityDTO;

public interface CommodityService {

    Long addCommodity(commodityDTO commodityDTO);

    commodityDTO getCommodityById(Long commodityId);

    commodityDTO updateCommodityById(Long id,String name,String price);

    void deleteCommodityById(Long id);
}
