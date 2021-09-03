package com.auction1_with_rabbitMQ.dao;

import com.auction1_with_rabbitMQ.configuration.SQLrequests;
import com.auction1_with_rabbitMQ.models.Customer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.auction1_with_rabbitMQ.consts.AttributesConsts.*;
import static com.auction1_with_rabbitMQ.consts.TypeConsts.CUSTOMER;


@Component
public class CustomerDAO {

    private final SQLrequests sqLrequests;
    private final LocationDAO locationDAO;

    public CustomerDAO(SQLrequests sqLrequests, LocationDAO locationDAO) {
        this.sqLrequests = sqLrequests;
        this.locationDAO = locationDAO;
    }


    public Customer createCustomer(JsonNode body) throws SQLException {
        Connection connection = this.sqLrequests.getConnection();

        ObjectMapper objectMapper = new ObjectMapper();

        Customer customer = objectMapper.convertValue(body, Customer.class);

        Savepoint savepointOne = null;
        try {
            savepointOne = connection.setSavepoint("SavepointOne");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        try {
            Statement statement = connection.createStatement();

            customer.setId(CUSTOMER);
            ResultSet max_object_id = statement.executeQuery("SELECT MAX(object_id) from objects");
            if (max_object_id.next()) {
                int OBJECT_ID = (max_object_id.getInt("max") + 1);

                statement.executeUpdate("INSERT into objects (object_id, object_type_id, name) VALUES ( default, \'" +
                        customer.getId() + "\' , \'" + "customer" + "\'" + "\'" + OBJECT_ID + "\'" + ")");

                ResultSet findId = statement.executeQuery("SELECT object_id from objects where object_type_id = \'" +
                        customer.getId() + "\' and name =  \'" + "customer" + "\'" + "\'" + OBJECT_ID + "\'");

                if (findId.next()) {
                    customer.setId(findId.getInt("object_id"));
                }

                String SQLsting = "INSERT into params(attribute_id, object_id, string_value) VALUES (?,?,?)";
                String SQLdate = "INSERT into params" +
                        " (attribute_id, object_id, date_value) VALUES (?,?,?)";

                PreparedStatement statement1 = connection.prepareStatement(SQLsting);

                statement1.setInt(1, SURNAME);
                statement1.setInt(2, customer.getId());
                statement1.setString(3, customer.getSurname());

                statement1.addBatch();

                statement1.setInt(1, NAME);
                statement1.setInt(2, customer.getId());
                statement1.setString(3, customer.getName());

                statement1.addBatch();

                statement1.setInt(1, PATRONYMIC);
                statement1.setInt(2, customer.getId());
                statement1.setString(3, customer.getPatronymic());

                statement1.addBatch();

                statement1.executeBatch();

                statement1 = connection.prepareStatement(SQLdate);

                statement1.setInt(1, DATE_OF_BIRTH);
                statement1.setInt(2, customer.getId());
                statement1.setDate(3, customer.getDateOfBirth());

                statement1.executeUpdate();

                connection.commit();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            try {
                connection.rollback(savepointOne);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        connection.close();
        return customer;
    }

    public Customer getCustomer(int id) throws SQLException {

        Connection connection = this.sqLrequests.getConnection();

        Customer customer = new Customer();
        customer.setId(id);


        String SQLstring = "select string_value from params WHERE object_id = ? " +
                "and attribute_id = ? ";
        String SQLdate = "select date_value from params WHERE object_id = ? " +
                "and attribute_id = ? ";

        PreparedStatement statement1 = connection.prepareStatement(SQLstring);
        PreparedStatement statement2 = connection.prepareStatement(SQLstring);
        PreparedStatement statement3 = connection.prepareStatement(SQLstring);
        PreparedStatement statement4 = connection.prepareStatement(SQLdate);

        statement1.setInt(1, id);
        statement1.setInt(2, SURNAME);

        statement2.setInt(1, id);
        statement2.setInt(2, NAME);

        statement3.setInt(1, id);
        statement3.setInt(2, PATRONYMIC);

        statement4.setInt(1, id);
        statement4.setInt(2, DATE_OF_BIRTH);


        ResultSet resultSet1 = statement1.executeQuery();
        ResultSet resultSet2 = statement2.executeQuery();
        ResultSet resultSet3 = statement3.executeQuery();
        ResultSet resultSet4 = statement4.executeQuery();


        if (resultSet1.next() & resultSet2.next() & resultSet3.next() & resultSet4.next()) {
            customer.setSurname(resultSet1.getString("string_value"));
            customer.setName(resultSet2.getString("string_value"));
            customer.setPatronymic(resultSet3.getString("string_value"));
            customer.setDateOfBirth(resultSet4.getDate("date_value"));
        }
        connection.close();
        return customer;
    }


    public List<Customer> getAllCustomers() throws SQLException {

        Connection connection = this.sqLrequests.getConnection();

        List<Customer> customers = new ArrayList<>();

        Statement statement1 = connection.createStatement();

        ResultSet allCustomers = statement1.executeQuery(
                "SELECT Count(*) from objects WHERE object_type_id = \'" + CUSTOMER + "\'");

        if (allCustomers.next()) {
            int[] count = new int[allCustomers.getInt("count")];

            ResultSet customersId = statement1.executeQuery(
                    "SELECT object_id from objects where object_type_id = \'" + CUSTOMER + "\'");

            int i = 0;
            while (customersId.next()) {
                count[i] = customersId.getInt("object_id");
                i++;
            }

            for (int j = 0; j < count.length; j++) {
                customers.add(getCustomer(count[j]));
            }
        }

        connection.close();
        return customers;
    }
}
