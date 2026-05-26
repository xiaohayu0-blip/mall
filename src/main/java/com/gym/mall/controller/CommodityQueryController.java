package com.gym.mall.controller;

import com.gym.mall.domain.dto.CommodityPageResponse;
import com.gym.mall.service.CommodityService;
import com.gym.mall.validator.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/commodities")
public class CommodityQueryController {

    @Autowired
    private CommodityService commodityService;

    @GetMapping("/by-tags")
    public Response<CommodityPageResponse> queryByTags(
            @RequestParam List<Long> tagIds,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        try {
            CommodityPageResponse response = commodityService.queryCommoditiesByTags(tagIds, page, pageSize);
            return Response.newSuccess(response);
        } catch (Exception e) {
            return Response.newFail("查询失败: " + e.getMessage());
        }
    }
}
