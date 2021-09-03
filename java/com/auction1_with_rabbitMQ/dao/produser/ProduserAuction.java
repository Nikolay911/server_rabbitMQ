package com.auction1_with_rabbitMQ.dao.produser;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProduserAuction {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private GeneralProduser generalProduser;

    public void GetAuction(String response){
        rabbitTemplate.convertAndSend(generalProduser.getQueue2(), response);
    }

    public void CreateAuction(String response){
        rabbitTemplate.convertAndSend(generalProduser.getQueue2(), response);
    }

}
