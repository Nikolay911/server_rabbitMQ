package com.auction1_with_rabbitMQ.mq_services.produser;

import com.auction1_with_rabbitMQ.models.Product;
import org.springframework.amqp.AmqpException;
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

    public void GetProduct(Product product){
        try {
            rabbitTemplate.convertAndSend(generalProduser.getQueue2(), product.toString());
        }
        catch (AmqpException the_exception){
            System.out.println("В соединении отказано. Проблема возникает при попытке подключиться к RabbitMQ");
        }
        catch (Exception ex){
            System.out.println("Неопознанное исключение");
        }
    }
}
