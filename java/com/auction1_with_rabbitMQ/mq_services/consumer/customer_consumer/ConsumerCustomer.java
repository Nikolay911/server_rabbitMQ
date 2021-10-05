package com.auction1_with_rabbitMQ.mq_services.consumer.customer_consumer;

import com.auction1_with_rabbitMQ.dao.CustomerDAO;
import com.auction1_with_rabbitMQ.models.Customer;
import com.auction1_with_rabbitMQ.mq_services.produser.ProduserCustomer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ConsumerCustomer {

    private final CustomerDAO customerDAO;
    private final ProduserCustomer produserCustomer;

    public ConsumerCustomer(CustomerDAO customerDAO, ProduserCustomer produserCustomer) {
        this.customerDAO = customerDAO;
        this.produserCustomer = produserCustomer;
    }

    public void GetAllCustomer(String str) throws SQLException {

        List<Customer> listCustomer = customerDAO.getAllCustomers();

        List<JSONObject> listJsonCustomer = new ArrayList<>();

        int i = 0;
        while (i < listCustomer.size()) {

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", listCustomer.get(i).getId());
            jsonObject.put("name", listCustomer.get(i).getName());
            jsonObject.put("surname", listCustomer.get(i).getSurname());
            jsonObject.put("patronymic", listCustomer.get(i).getPatronymic());
            jsonObject.put("dateOfBirth", listCustomer.get(i).getDateOfBirth().toString());

            listJsonCustomer.add(jsonObject);
            i++;
        }

        produserCustomer.GetAllCustomer(listJsonCustomer.toString());
    }

    public void GetCustomerById(int customerId) throws SQLException {

        Customer customer = customerDAO.getCustomer(customerId);
        produserCustomer.GetCustomer(customer);
        System.out.println(customer);
    }

    public void CreateCustomer(com.functions.CreateCustomer createCustomer) throws SQLException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", createCustomer.getName());
        jsonObject.put("surname", createCustomer.getSurname());
        jsonObject.put("patronymic", createCustomer.getPatronymic());
        jsonObject.put("dateOfBirth", createCustomer.getDateOfBirth());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.convertValue(jsonObject, JsonNode.class);

        Customer customer = customerDAO.createCustomer(jsonNode);
        produserCustomer.CreateCustomer(customer);
    }






































    /*@RabbitHandler
    public void GetCustomer(String msg) throws InterruptedException, SQLException, ParseException {
        //Thread.sleep(10000);

            JSONParser parser = new JSONParser();
            JSONObject jsonMsg = (JSONObject) parser.parse(msg);

            *//*if (jsonMsg.get("requestName").toString().equals("GetCustomer")) {*//*
                int customerId = Integer.parseInt(jsonMsg.get("CustomerId").toString());
                Customer customer = customerDAO.getCustomer(customerId);
                System.out.println(customerId);
                produserCustomer.GetCustomer(customer);

*//*            } else {

                List<Customer> listCustomer = customerDAO.getAllCustomers();

                List<JSONObject> listJsonCustomer = new ArrayList<>();

                int i = 0;
                while (i < listCustomer.size()) {

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", listCustomer.get(i).getId());
                    jsonObject.put("name", listCustomer.get(i).getName());
                    jsonObject.put("surname", listCustomer.get(i).getSurname());
                    jsonObject.put("patronymic", listCustomer.get(i).getPatronymic());
                    jsonObject.put("dateOfBirth", listCustomer.get(i).getDateOfBirth().toString());

                    listJsonCustomer.add(jsonObject);
                    i++;
                }

                produserCustomer.GetAllCustomer(listJsonCustomer.toString());
                System.out.println("mesage: " + msg);
            }
        }*//*
    }*/

/*    public class MessagesHandler implements MessageListener {

        public void onMessage(Message message) {
            Map<String, Object> headers = message.getMessageProperties().getHeaders();
            for (Map.Entry<String, Object> header : headers.entrySet())
            {
                System.out.println(header.getKey() + " : " + header.getValue());
            }
        }
    }*/

/*        @RabbitHandler
        public void GetAllCustomer(@Header("headerKey1") @Payload String tag, @Payload String str) throws SQLException {

            List<Customer> listCustomer = customerDAO.getAllCustomers();

            List<JSONObject> listJsonCustomer = new ArrayList<>();

            int i = 0;
            while (i < listCustomer.size()) {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", listCustomer.get(i).getId());
                jsonObject.put("name", listCustomer.get(i).getName());
                jsonObject.put("surname", listCustomer.get(i).getSurname());
                jsonObject.put("patronymic", listCustomer.get(i).getPatronymic());
                jsonObject.put("dateOfBirth", listCustomer.get(i).getDateOfBirth().toString());

                listJsonCustomer.add(jsonObject);
                i++;
            }

            produserCustomer.GetAllCustomer(listJsonCustomer.toString());
            System.out.println("mesage: " +tag+ "Эшшш");
        }

    @RabbitHandler
    public void GetCustomerById(@Header("headerKey2") @Payload String tag, @Payload String str) throws SQLException {

        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jsonObject = objectMapper.convertValue("Эшшш", JSONObject.class);

        Customer customer = customerDAO.getCustomer((int)jsonObject.get("CustomerId"));
        System.out.println(customer);
    }*/
}
