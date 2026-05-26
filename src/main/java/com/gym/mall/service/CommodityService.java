package com.gym.mall.service;

import com.gym.mall.domain.dto.CommodityPageRequest;
import com.gym.mall.domain.dto.CommodityPageResponse;
import com.gym.mall.domain.dto.commodityDTO;

import java.util.List;

public interface CommodityService {

    Long addCommodity(commodityDTO commodityDTO);

    commodityDTO getCommodityById(Long commodityId);

    commodityDTO updateCommodityById(Long id, String name, String price, Long categoryId, String description, Integer stock);

    void deleteCommodityById(Long id);

    CommodityPageResponse queryCommodities(CommodityPageRequest pageRequest);

    commodityDTO bindTagsToCommodity(Long commodityId, List<Long> tagIds);

    void unbindTagFromCommodity(Long commodityId, Long tagId);

    List<commodityDTO> getCommoditiesByTagId(Long tagId);

    CommodityPageResponse queryCommoditiesByTags(List<Long> tagIds, Integer page, Integer pageSize);

}