package com.auction1_with_rabbitMQ.mq_services.produser;

import com.auction1_with_rabbitMQ.models.Customer;
import org.springframework.amqp.AmqpException;
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

    public void GetCustomer(Customer customer){
        try {
            rabbitTemplate.convertAndSend(generalProduser.getQueue2(), customer.toString());
        }
        catch (AmqpException the_exception){
            System.out.println("В соединении отказано. Проблема возникает при попытке подключиться к RabbitMQ");
        }
        catch (Exception ex){
            System.out.println("Неопознанное исключение");
        }
    }

    public void CreateCustomer(Customer customer){
        try {
            rabbitTemplate.convertAndSend(generalProduser.getQueue2(), customer.toString());
        }
        catch (AmqpException the_exception){
            System.out.println("В соединении отказано. Проблема возникает при попытке подключиться к RabbitMQ");
        }
        catch (Exception ex){
            System.out.println("Неопознанное исключение");
        }
    }
}
