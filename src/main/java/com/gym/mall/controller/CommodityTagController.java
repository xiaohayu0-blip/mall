package com.gym.mall.controller;

import com.gym.mall.domain.dto.commodityDTO;
import com.gym.mall.interceptor.AdminOnly;
import com.gym.mall.service.CommodityService;
import com.gym.mall.validator.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/commodity/{commodityId}/tags")
public class CommodityTagController {

    @Autowired
    private CommodityService commodityService;

    @AdminOnly
    @PostMapping("/bind")
    public Response<commodityDTO> bindTags(@PathVariable Long commodityId,
                                           @RequestBody List<Long> tagIds){
        try {
            commodityDTO result=commodityService.bindTagsToCommodity(commodityId,tagIds);
            return Response.newSuccess(result);
        } catch (Exception e) {
            return Response.newFail("解绑失败: " + e.getMessage());
        }
    }

    @AdminOnly
    @DeleteMapping("/{tagId}/unbind")
    public Response<String> unbindTag(@PathVariable Long commodityId, @PathVariable Long tagId) {
        try {
            commodityService.unbindTagFromCommodity(commodityId, tagId);
            return Response.newSuccess("解绑成功");
        } catch (Exception e) {
            return Response.newFail("解绑失败: " + e.getMessage());
        }
    }

    @GetMapping("/{tagId}")
    public Response<List<commodityDTO>> getCommoditiesByTag(@PathVariable Long tagId) {
        List<commodityDTO> commodities = commodityService.getCommoditiesByTagId(tagId);
        return Response.newSuccess(commodities);
    }
}
