package com.gym.mall.controller;

import com.gym.mall.service.OrderService;
import com.gym.mall.validator.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 模拟支付控制器
 *
 * 真实支付流程：前端调起支付页面 → 用户扫码/输入密码 → 支付平台回调后端
 * 模拟支付流程：前端请求本接口 → 后端直接将订单状态改为已支付
 *
 * 知识点：
 * - 支付状态机：待支付 → 已支付
 * - 幂等性：同一订单多次调用不会重复扣款
 * - 订单超时取消：30 分钟内未支付自动取消（可配合定时任务）
 */
@RestController
@RequestMapping("/payment")
@Slf4j
public class PaymentController {

    @Autowired
    private OrderService orderService;

    /**
     * 模拟支付
     *
     * @param orderNo 订单号
     * @return 支付结果
     */
    @PostMapping("/mock")
    public Response<String> mockPay(@RequestParam String orderNo) {
        try {
            orderService.payOrder(orderNo);
            log.info("模拟支付成功, orderNo: {}", orderNo);
            return Response.newSuccess("支付成功");
        } catch (Exception e) {
            log.error("模拟支付失败, orderNo: {}", orderNo, e);
            return Response.newFail("支付失败: " + e.getMessage());
        }
    }
}
