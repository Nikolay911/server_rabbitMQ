package com.auction1_with_rabbitMQ.mq_services.produser;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProduserLocation {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private GeneralProduser generalProduser;

    public void CreateLocationToCustomer(String response){
        try {
            rabbitTemplate.convertAndSend(generalProduser.getQueue2(), response);
        }
        catch (AmqpException the_exception){
            System.out.println("В соединении отказано. Проблема возникает при попытке подключиться к RabbitMQ");
        }
        catch (Exception ex){
            System.out.println("Неопознанное исключение");
        }
    }
}
