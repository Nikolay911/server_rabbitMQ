package com.auction1_with_rabbitMQ.dao.produser;

import com.auction1_with_rabbitMQ.models.Product;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProduserProduct {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private GeneralProduser generalProduser;

    public void CreateProduct(String response){
        rabbitTemplate.convertAndSend(generalProduser.getQueue2(), response);
    }

    public void GetProduct(Product product){
        rabbitTemplate.convertAndSend(generalProduser.getQueue2(), product.toString());
    }
}
