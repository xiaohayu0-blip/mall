package com.gym.mall.controller;

import com.gym.mall.domain.dto.CommodityPageRequest;
import com.gym.mall.domain.dto.CommodityPageResponse;
import com.gym.mall.domain.dto.commodityDTO;
import com.gym.mall.service.CommodityService;
import com.gym.mall.validator.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class commodityController {

    @Autowired
    private CommodityService commodityService;

    @PostMapping("/commodity")
    public Response<Long> addCommodity(@RequestBody commodityDTO commodityDTO){
        return Response.newSuccess(commodityService.addCommodity(commodityDTO));
    }

    @GetMapping("/commodity/{id}")
    public Response<commodityDTO> getCommodityById(@PathVariable Long id){
        return Response.newSuccess(commodityService.getCommodityById(id));
    }

    @GetMapping("/commodity/page")
    public Response<CommodityPageResponse> queryCommodities(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        CommodityPageRequest pageRequest = new CommodityPageRequest();
        pageRequest.setPage(page);
        pageRequest.setPageSize(pageSize);
        pageRequest.setKeyword(keyword);
        pageRequest.setCategoryId(categoryId);
        return Response.newSuccess(commodityService.queryCommodities(pageRequest));
    }

    @PutMapping("/commodity/{id}")
    public Response<commodityDTO> updateCommodityById(@PathVariable long id,
                                                      @RequestParam(required = false) String name,
                                                      @RequestParam(required= false) String price,
                                                      @RequestParam(required = false) Long categoryId,
                                                      @RequestParam(required = false) String description,
                                                      @RequestParam(required = false) Integer stock){
        return Response.newSuccess(commodityService.updateCommodityById(id, name, price, categoryId, description, stock));
    }

    @DeleteMapping("/commodity/{id}")
    public Response<String> deleteCommodityById(@PathVariable Long id){
        try {
            commodityService.deleteCommodityById(id);
            return Response.newSuccess("删除成功");
        } catch (Exception e) {
            return Response.newFail("删除失败");
        }
    }
}