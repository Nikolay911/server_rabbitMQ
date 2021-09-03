package com.auction1_with_rabbitMQ.dao.consumer;

import com.auction1_with_rabbitMQ.dao.AuctionDAO;
import com.auction1_with_rabbitMQ.dao.produser.ProduserAuction;
import com.auction1_with_rabbitMQ.models.AuctionModel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class ConsumerAuction {

    private final AuctionDAO AuctionDAO;
    private final ProduserAuction produserAuction;

    public ConsumerAuction(com.auction1_with_rabbitMQ.dao.AuctionDAO auctionDAO, ProduserAuction produserAuction) {
        AuctionDAO = auctionDAO;
        this.produserAuction = produserAuction;
    }

    public void CreateAuction(String str) throws Exception {

        JSONParser parser = new JSONParser();

        JSONObject jsonObject = (JSONObject) parser.parse(str);

        int customerId = Integer.parseInt(jsonObject.get("customerId").toString());
        int productId = Integer.parseInt(jsonObject.get("productId").toString());

        jsonObject.remove("customerId");
        jsonObject.remove("productId");

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.convertValue(jsonObject, JsonNode.class);
        //System.out.println(jsonNode.toString());
        AuctionModel auction = AuctionDAO.createAuction(customerId, productId, jsonNode);
        produserAuction.CreateAuction(auction.toString());

    }

    public void GetAuction(String str) throws ParseException, SQLException {

        JSONParser parser = new JSONParser();

        JSONObject jsonObject = (JSONObject) parser.parse(str);

        int auctionId = Integer.parseInt(jsonObject.get("auctionId").toString());
        AuctionModel auctionModel = AuctionDAO.getAuction(auctionId);
        produserAuction.GetAuction(auctionModel.toString());

    }

}
