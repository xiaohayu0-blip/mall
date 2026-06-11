package com.gym.mall.controller;

import com.gym.mall.domain.dto.CommodityPageRequest;
import com.gym.mall.domain.dto.CommodityPageResponse;
import com.gym.mall.domain.dto.CommodityDTO;
import com.gym.mall.interceptor.AdminOnly;
import com.gym.mall.service.CommodityService;
import com.gym.mall.validator.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "商品管理", description = "商品的增删改查及上下架")
@RestController
public class CommodityController {

    @Autowired
    private CommodityService commodityService;

    @Operation(summary = "添加商品", description = "管理员权限")
    @AdminOnly
    @PostMapping("/commodity")
    public Response<Long> addCommodity(@Valid @RequestBody CommodityDTO commodityDTO) {
        return Response.newSuccess(commodityService.addCommodity(commodityDTO));
    }

    @Operation(summary = "根据ID查询商品")
    @GetMapping("/commodity/{id}")
    public Response<CommodityDTO> getCommodityById(@Parameter(description = "商品ID") @PathVariable Long id) {
        return Response.newSuccess(commodityService.getCommodityById(id));
    }

    @Operation(summary = "分页查询商品列表", description = "支持关键字搜索和分类过滤")
    @GetMapping("/commodity/page")
    public Response<CommodityPageResponse> queryCommodities(
            @Parameter(description = "页码，默认1") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页数量，默认10") @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId) {
        CommodityPageRequest pageRequest = new CommodityPageRequest();
        pageRequest.setPage(page);
        pageRequest.setPageSize(pageSize);
        pageRequest.setKeyword(keyword);
        pageRequest.setCategoryId(categoryId);
        return Response.newSuccess(commodityService.queryCommodities(pageRequest));
    }

    @Operation(summary = "修改商品信息", description = "管理员权限，仅传入需要修改的字段")
    @AdminOnly
    @PutMapping("/commodity/{id}")
    public Response<CommodityDTO> updateCommodityById(
            @Parameter(description = "商品ID") @PathVariable long id,
            @Parameter(description = "商品名称") @RequestParam(required = false) String name,
            @Parameter(description = "价格（分）") @RequestParam(required = false) Long price,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "描述") @RequestParam(required = false) String description,
            @Parameter(description = "库存") @RequestParam(required = false) Integer stock,
            @Parameter(description = "上架状态：1=上架, 0=下架") @RequestParam(required = false) Integer status) {
        return Response.newSuccess(commodityService.updateCommodityById(id, name, price, categoryId, description, stock, status));
    }

    @Operation(summary = "删除商品", description = "管理员权限")
    @AdminOnly
    @DeleteMapping("/commodity/{id}")
    public Response<String> deleteCommodityById(@Parameter(description = "商品ID") @PathVariable Long id) {
        commodityService.deleteCommodityById(id);
        return Response.newSuccess("删除成功");
    }

    @Operation(summary = "切换商品上架/下架状态", description = "管理员权限")
    @AdminOnly
    @PutMapping("/commodity/{id}/status")
    public Response<CommodityDTO> toggleCommodityStatus(
            @Parameter(description = "商品ID") @PathVariable Long id,
            @Parameter(description = "1=上架, 0=下架") @RequestParam Integer status) {
        return Response.newSuccess(commodityService.updateCommodityById(id, null, null, null, null, null, status));
    }
}