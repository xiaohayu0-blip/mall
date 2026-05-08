package com.gym.mall.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitmqConfig {

    @Value("${rabbitmq.commodity.queue}")
    private String queueName;

    @Value("${rabbitmq.commodity.exchange}")
    private String exchange;

    @Value("${rabbitmq.commodity.routingkey}")
    private String routingkey;

    @Value("${rabbitmq.commodity.dlq.queue:commodity.likes.dlq.queue}")
    private String dlqQueueName;

    @Value("${rabbitmq.commodity.dlq.exchange:commodity.likes.dlq.exchange}")
    private String dlqExchange;

    @Value("${rabbitmq.commodity.dlq.routingkey:commodity.likes.dlq.routingkey}")
    private String dlqRoutingkey;

    @Bean
    Queue queue() {
        Map<String,Object> params = new HashMap<>();
        params.put("x-dead-letter-exchange", dlqExchange);
        params.put("x-dead-letter-routing-key", dlqRoutingkey);
        return QueueBuilder.durable(queueName).withArguments(params).build();
    }

    @Bean
    DirectExchange exchange() {
        return new DirectExchange(exchange);
    }

    @Bean
    Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingkey);
    }

    @Bean
    Queue dlqQueue() {
        return new Queue(dlqQueueName);
    }

    @Bean
    public DirectExchange dlqExchange(){
        return new DirectExchange(dlqExchange);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    Binding dlqBinding(Queue dlqQueue, DirectExchange dlqExchange) {
        return BindingBuilder.bind(dlqQueue).to(dlqExchange).with(dlqRoutingkey);
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

}
