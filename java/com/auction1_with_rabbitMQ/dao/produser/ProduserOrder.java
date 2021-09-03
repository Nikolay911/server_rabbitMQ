package com.auction1_with_rabbitMQ.dao.produser;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProduserOrder {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private GeneralProduser generalProduser;

    public void Bid(String response){
        rabbitTemplate.convertAndSend(generalProduser.getQueue2(), response);
    }

}
