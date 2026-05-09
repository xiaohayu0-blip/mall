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
/**
 * RabbitMQ 配置类
 * 用于定义队列、交换机、绑定关系以及消息转换器
 */
public class RabbitmqConfig {

    // 从 application.properties 中读取配置的属性值
    @Value("${rabbitmq.commodity.queue}")
    private String queueName; // 主队列名称

    @Value("${rabbitmq.commodity.exchange}")
    private String exchange; // 主交换机名称

    @Value("${rabbitmq.commodity.routingkey}")
    private String routingkey; // 路由键（暗号）

    // 死信队列（Dead Letter Queue）相关配置：处理失败的消息
    @Value("${rabbitmq.commodity.dlq.queue:commodity.likes.dlq.queue}")
    private String dlqQueueName;

    @Value("${rabbitmq.commodity.dlq.exchange:commodity.likes.dlq.exchange}")
    private String dlqExchange;

    @Value("${rabbitmq.commodity.dlq.routingkey:commodity.likes.dlq.routingkey}")
    private String dlqRoutingkey;

    /**
     * 定义主队列
     * 配置了死信交换机，如果消息在主队列处理失败，会被投递到死信队列
     */
    @Bean
    Queue queue() {
        Map<String,Object> params = new HashMap<>();
        // 设置消息处理失败时转发到的交换机
        params.put("x-dead-letter-exchange", dlqExchange);
        // 设置转发时使用的路由键
        params.put("x-dead-letter-routing-key", dlqRoutingkey);
        return QueueBuilder.durable(queueName).withArguments(params).build();
    }

    /**
     * 定义主交换机（直连型交换机）
     */
    @Bean
    DirectExchange exchange() {
        return new DirectExchange(exchange);
    }

    /**
     * 将主队列绑定到主交换机上，并指定路由键
     */
    @Bean
    Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingkey);
    }

    /**
     * 定义死信队列（回收站）
     */
    @Bean
    Queue dlqQueue() {
        return new Queue(dlqQueueName);
    }

    /**
     * 定义死信交换机
     */
    @Bean
    public DirectExchange dlqExchange(){
        return new DirectExchange(dlqExchange);
    }

    /**
     * 配置消息转换器
     * 将 Java 对象转换为 JSON 格式发送，方便跨语言和易读性
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 将死信队列绑定到死信交换机上
     */
    @Bean
    Binding dlqBinding(Queue dlqQueue, DirectExchange dlqExchange) {
        return BindingBuilder.bind(dlqQueue).to(dlqExchange).with(dlqRoutingkey);
    }

    /**
     * 配置 RabbitTemplate
     * 这是 Spring 提供操作 RabbitMQ 的核心工具类，设置了上面定义的 JSON 转换器
     */
    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

}
