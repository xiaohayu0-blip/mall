package com.gym.mall.controller;

import com.gym.mall.domain.dto.CommodityPageResponse;
import com.gym.mall.domain.dto.CommoditySearchRequest;
import com.gym.mall.service.CommoditySearchService;
import com.gym.mall.validator.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "商品搜索", description = "基于 MySQL FULLTEXT 索引的商品全文搜索")
@RestController
@RequestMapping("/commodity")
public class CommoditySearchController {

    @Autowired
    private CommoditySearchService commoditySearchService;

    @Operation(summary = "商品全文搜索", description = "支持关键字（FULLTEXT）、分类、价格区间过滤，支持分页")
    @GetMapping("/search")
    public Response<CommodityPageResponse> search(
            @Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "最低价格（分）") @RequestParam(required = false) Long minPrice,
            @Parameter(description = "最高价格（分）") @RequestParam(required = false) Long maxPrice,
            @Parameter(description = "页码，默认1") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页数量，默认10") @RequestParam(required = false, defaultValue = "10") Integer pageSize) {

        CommoditySearchRequest request = CommoditySearchRequest.builder()
                .keyword(keyword)
                .categoryId(categoryId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .page(page)
                .pageSize(pageSize)
                .build();

        return Response.newSuccess(commoditySearchService.search(request));
    }
}
