package com.example.springbootdemo.common.rabbit;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

//    // 声明队列
//    @Bean
//    public Queue myQueue() {
//        return new Queue("myQueue", true); // 持久化队列
//    }
//
//    // 声明直连交换机
//    @Bean
//    public DirectExchange myExchange() {
//        return new DirectExchange("myExchange");
//    }
//
//    // 绑定队列到交换机
//    @Bean
//    public Binding binding(Queue myQueue, DirectExchange myExchange) {
//        return BindingBuilder.bind(myQueue).to(myExchange).with("myRoutingKey");
//    }
//
//    // 配置JSON消息转换器
//    @Bean
//    public MessageConverter jsonMessageConverter() {
//        return new Jackson2JsonMessageConverter();
//    }
}