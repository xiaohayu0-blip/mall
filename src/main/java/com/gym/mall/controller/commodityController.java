package com.gym.mall.controller;

import com.gym.mall.dto.commodityDTO;
import com.gym.mall.service.CommodityService;
import com.gym.mall.service.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
//自动JSON转换：方法的返回值会自动转换为JSON格式
//HTTP响应：直接处理HTTP请求和响应，不需要返回视图页面
public class commodityController {

    @Autowired
    //自动注入依赖
    //实现 控制反转（IoC）和 依赖注入（DI）
    private CommodityService commodityService;

    @PostMapping("/commodity")
    //映射 HTTP POST 请求：将特定的 POST 请求路由到对应的处理方法
    public Response<Long> addCommodity(@RequestBody commodityDTO commodityDTO){
        //接收请求体中的 JSON/XML 数据
        //将 HTTP 请求体中的数据自动绑定到 Java 对象上
        return Response.newSuccess(commodityService.addCommodity(commodityDTO));
    }
}
