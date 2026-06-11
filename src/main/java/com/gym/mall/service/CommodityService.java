package com.gym.mall.service;

import com.gym.mall.domain.dto.CommodityPageRequest;
import com.gym.mall.domain.dto.CommodityPageResponse;
import com.gym.mall.domain.dto.CommodityDTO;

import java.util.List;

public interface CommodityService {

    Long addCommodity(CommodityDTO commodityDTO);

    CommodityDTO getCommodityById(Long commodityId);

    CommodityDTO updateCommodityById(Long id, String name, Long price, Long categoryId, String description, Integer stock);

    CommodityDTO updateCommodityById(Long id, String name, Long price, Long categoryId, String description, Integer stock, Integer status);

    void deleteCommodityById(Long id);

    CommodityPageResponse queryCommodities(CommodityPageRequest pageRequest);

    CommodityDTO bindTagsToCommodity(Long commodityId, List<Long> tagIds);

    void unbindTagFromCommodity(Long commodityId, Long tagId);

    List<CommodityDTO> getCommoditiesByTagId(Long tagId);

    CommodityPageResponse queryCommoditiesByTags(List<Long> tagIds, Integer page, Integer pageSize);

}