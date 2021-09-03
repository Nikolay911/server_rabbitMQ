package com.auction1_with_rabbitMQ.dao.produser;

import com.auction1_with_rabbitMQ.models.Customer;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProduserCustomer {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private GeneralProduser generalProduser;

    public void GetAllCustomer(String response){
        rabbitTemplate.convertAndSend(generalProduser.getQueue2(), response);
    }

    public void GetCustomer(Customer customer){
        rabbitTemplate.convertAndSend(generalProduser.getQueue2(), customer.toString());
    }

    public void CreateCustomer(Customer customer){
        rabbitTemplate.convertAndSend(generalProduser.getQueue2(), customer.toString());
    }
}
