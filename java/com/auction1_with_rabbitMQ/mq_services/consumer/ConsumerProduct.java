package com.auction1_with_rabbitMQ.mq_services.consumer;

import com.auction1_with_rabbitMQ.dao.ProductDAO;
import com.auction1_with_rabbitMQ.mq_services.produser.ProduserProduct;
import com.auction1_with_rabbitMQ.models.Product;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class ConsumerProduct {

    private final ProductDAO productDAO;
    private final ProduserProduct produserProduct;


    public ConsumerProduct(ProductDAO productDAO, ProduserProduct produserProduct) {
        this.productDAO = productDAO;
        this.produserProduct = produserProduct;
    }

    public void CreateProduct(String str) throws ParseException, SQLException {

        JSONParser parser = new JSONParser();

        JSONObject jsonObject = (JSONObject) parser.parse(str);

        int productId = Integer.parseInt(jsonObject.get("id").toString());

        jsonObject.remove("id");

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.convertValue(jsonObject, JsonNode.class);
        System.out.println(jsonNode.toString());
        Product product = productDAO.createProduct(productId, jsonNode);
        produserProduct.CreateProduct(product.toString());
    }

    public void GetProduct(String str) throws ParseException, SQLException {

        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(str);

        Product product = productDAO.getProduct(Integer.parseInt(jsonObject.get("id").toString()));
        produserProduct.GetProduct(product);
    }

}
