package com.gym.mall.service;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitmqService {
    @Autowired
    private AmqpTemplate amqpTemplate;

    @Value("${rabbitmq.reading.exchange}")
    private String exchange;

    @Value("${rabbitmq.reading.routingkey}")
    private String routingkey;

    public <T> void publishMessage(T type){
        amqpTemplate.convertAndSend(exchange, routingkey, type);
    }

}
