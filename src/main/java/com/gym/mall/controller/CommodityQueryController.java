package com.gym.mall.controller;

import com.gym.mall.domain.dto.CommodityPageResponse;
import com.gym.mall.service.CommodityService;
import com.gym.mall.validator.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "商品查询（标签维度）", description = "按标签筛选商品")
@RestController
@RequestMapping("/commodities")
public class CommodityQueryController {

    @Autowired
    private CommodityService commodityService;

    @Operation(summary = "按标签列表分页查询商品")
    @GetMapping("/by-tags")
    public Response<CommodityPageResponse> queryByTags(
            @Parameter(description = "标签ID列表") @RequestParam List<Long> tagIds,
            @Parameter(description = "页码") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        CommodityPageResponse response = commodityService.queryCommoditiesByTags(tagIds, page, pageSize);
        return Response.newSuccess(response);
    }
}
