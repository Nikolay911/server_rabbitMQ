package com.auction1_with_rabbitMQ.dao.consumer;


import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "${rabbitmq.queue}")
public class GeneralConsumer {

    private final ConsumerCustomer customer;
    private final ConsumerLocation location;
    private final ConsumerProduct product;
    private final ConsumerAuction auction;
    private final ConsumerOrder order;

    public GeneralConsumer(ConsumerCustomer customer, ConsumerLocation location, ConsumerProduct product, ConsumerAuction auction, ConsumerOrder order) {
        this.customer = customer;
        this.location = location;
        this.product = product;
        this.auction = auction;
        this.order = order;
    }

    @RabbitHandler
    public void GetAllCustomer(@Header("headerKey2") String head, @Payload String str) throws Exception {

        switch (head){
            case "GetAllCustomer":
                customer.GetAllCustomer(str);
                break;
            case "GetCustomerById":
                customer.GetCustomerById(str);
                break;
            case "createCustomer":
                customer.CreateCustomer(str);
                break;
            case "CreateLocationToCustomer":
                location.CreateLocation(str);
                break;
            case "CreateProduct":
                product.CreateProduct(str);
                break;
            case "GetProduct":
                product.GetProduct(str);
                break;
            case "CreateAuction":
                auction.CreateAuction(str);
                break;
            case "GetAuction":
                auction.GetAuction(str);
                break;
            case "Bid":
                order.Bid(str);
                break;
        }


    }

}
