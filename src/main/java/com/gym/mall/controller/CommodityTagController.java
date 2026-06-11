package com.gym.mall.controller;

import com.gym.mall.domain.dto.CommodityDTO;
import com.gym.mall.interceptor.AdminOnly;
import com.gym.mall.service.CommodityService;
import com.gym.mall.validator.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "商品-标签关联", description = "商品与标签的绑定/解绑")
@RestController
@RequestMapping("/commodity/{commodityId}/tags")
public class CommodityTagController {

    @Autowired
    private CommodityService commodityService;

    @Operation(summary = "批量绑定标签到商品", description = "管理员权限")
    @AdminOnly
    @PostMapping("/bind")
    public Response<CommodityDTO> bindTags(
            @Parameter(description = "商品ID") @PathVariable Long commodityId,
            @RequestBody List<Long> tagIds) {
        CommodityDTO result = commodityService.bindTagsToCommodity(commodityId, tagIds);
        return Response.newSuccess(result);
    }

    @Operation(summary = "解绑商品的某个标签", description = "管理员权限")
    @AdminOnly
    @DeleteMapping("/{tagId}/unbind")
    public Response<String> unbindTag(
            @Parameter(description = "商品ID") @PathVariable Long commodityId,
            @Parameter(description = "标签ID") @PathVariable Long tagId) {
        commodityService.unbindTagFromCommodity(commodityId, tagId);
        return Response.newSuccess("解绑成功");
    }

    @Operation(summary = "查询拥有指定标签的商品列表")
    @GetMapping("/{tagId}")
    public Response<List<CommodityDTO>> getCommoditiesByTag(
            @Parameter(description = "标签ID") @PathVariable Long tagId) {
        List<CommodityDTO> commodities = commodityService.getCommoditiesByTagId(tagId);
        return Response.newSuccess(commodities);
    }
}
