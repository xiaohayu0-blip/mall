package com.gym.mall.controller;

import com.gym.mall.service.OrderService;
import com.gym.mall.validator.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "模拟支付", description = "模拟支付流程（非真实支付）")
@RestController
@RequestMapping("/payment")
@Slf4j
public class PaymentController {

    @Autowired
    private OrderService orderService;

    @Operation(summary = "模拟支付", description = "直接将订单状态改为已支付，模拟支付回调")
    @PostMapping("/mock")
    public Response<String> mockPay(
            @Parameter(description = "订单号") @RequestParam String orderNo) {
        orderService.payOrder(orderNo);
        log.info("模拟支付成功, orderNo: {}", orderNo);
        return Response.newSuccess("支付成功");
    }
}
