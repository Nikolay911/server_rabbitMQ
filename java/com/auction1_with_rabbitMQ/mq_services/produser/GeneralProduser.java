package com.auction1_with_rabbitMQ.mq_services.produser;

import lombok.Data;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Data
@Component
public class GeneralProduser {

    @Value("${rabbitmq.exchange2}")
    private String exchange2;

    @Value("${rabbitmq.routingKey2}")
    private String routingKey2;

    @Value("${rabbitmq.queue2}")
    private String queue2;

    @Bean
    public TopicExchange exchange(){
        return new TopicExchange(exchange2);
    }

    @Bean
    public Queue queue(){
        return new Queue(queue2, false);
    }

    @Bean
    Binding binding(Queue queue, TopicExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(routingKey2);
    }
}
