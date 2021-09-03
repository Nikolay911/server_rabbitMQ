package com.auction1_with_rabbitMQ.dao.consumer;

import com.auction1_with_rabbitMQ.dao.OrderDAO;
import com.auction1_with_rabbitMQ.dao.produser.ProduserOrder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class ConsumerOrder {

    private final OrderDAO orderDAO;
    private final ProduserOrder produserOrder;

    public ConsumerOrder(OrderDAO orderDAO, ProduserOrder produserOrder) {
        this.orderDAO = orderDAO;
        this.produserOrder = produserOrder;
    }

    public void Bid(String str) throws ParseException, SQLException {

        JSONParser parser = new JSONParser();

        JSONObject jsonObject = (JSONObject) parser.parse(str);

        int customerId = Integer.parseInt(jsonObject.get("customerId").toString());
        int auctionId = Integer.parseInt(jsonObject.get("auctionId").toString());

        jsonObject.remove("auctionId");
        jsonObject.remove("customerId");

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.convertValue(jsonObject, JsonNode.class);
        String order = orderDAO.bid(customerId, auctionId, jsonNode);
        produserOrder.Bid(order);
    }
}
