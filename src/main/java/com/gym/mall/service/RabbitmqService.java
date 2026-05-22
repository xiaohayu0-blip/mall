package com.gym.mall.service;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitmqService {

    @Autowired
    private AmqpTemplate amqpTemplate;
    //Spring AMQP 提供的消息操作模板,Spring 对 RabbitMQ 的封装

    @Value("${rabbitmq.commodity.exchange}")
    private String exchange;

    @Value("${rabbitmq.commodity.routingkey}")
    private String routingkey;

    /**
     * 发送消息到 RabbitMQ
     * @param type 消息对象，会自动序列化为 JSON 发送
     * @param <T> 泛型，支持任意类型的消息对象
     */
    public <T> void publishMessage(T type){
        amqpTemplate.convertAndSend(exchange, routingkey, type);
    }

}
