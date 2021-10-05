package com.auction1_with_rabbitMQ.mq_services.consumer;

import com.auction1_with_rabbitMQ.dao.LocationDAO;
import com.auction1_with_rabbitMQ.mq_services.produser.ProduserLocation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class ConsumerLocation {

    private final LocationDAO locationDAO;
    private final ProduserLocation produserLocation;

    public ConsumerLocation(LocationDAO locationDAO, ProduserLocation produserLocation) {
        this.locationDAO = locationDAO;
        this.produserLocation = produserLocation;
    }

    public void CreateLocation(String str) throws ParseException, SQLException {

        JSONParser parser = new JSONParser();

        JSONObject jsonObject = (JSONObject) parser.parse(str);

        int customerId = Integer.parseInt(jsonObject.get("id").toString());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.convertValue(jsonObject, JsonNode.class);
        System.out.println(jsonNode.toString());
        Boolean bool = locationDAO.addLocationToCustomer(customerId, jsonNode);
        produserLocation.CreateLocationToCustomer(bool.toString());

    }
}
