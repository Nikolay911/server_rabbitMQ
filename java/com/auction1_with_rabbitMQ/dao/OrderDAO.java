package com.auction1_with_rabbitMQ.dao;

import com.auction1_with_rabbitMQ.configuration.SQLrequests;

import com.auction1_with_rabbitMQ.models.Customer;
import com.auction1_with_rabbitMQ.models.Order;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.auction1_with_rabbitMQ.consts.AttributesConsts.*;
import static com.auction1_with_rabbitMQ.consts.TypeConsts.ORDER;

@Component
public class OrderDAO {

    private final CustomerDAO customerDAO;
    private final SQLrequests sqLrequests;
    private final AuctionDAO auctionDAO;

    public OrderDAO(CustomerDAO customerDAO, SQLrequests sqLrequests, AuctionDAO auctionDAO){
        this.customerDAO = customerDAO;
        this.sqLrequests = sqLrequests;
        this.auctionDAO = auctionDAO;
    }


    public String bid(int idCustomer, int idAuction, JsonNode body) throws SQLException {

        Connection connection = this.sqLrequests.getConnection();

        ObjectMapper objectMapper = new ObjectMapper();
        Order order = objectMapper.convertValue(body, Order.class);

        if(auctionDAO.auctionIsExist(idAuction, connection) & !auctionDAO.auctionIsCompleted(idAuction, connection)
                & auctionDAO.auctionIsInProgress(idAuction, connection)){

            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            order.setBidDate(currentTime);

            Savepoint savepointOne = null;
            try {
                savepointOne = connection.setSavepoint("SavepointOne");
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }


            try{
                Statement statement = connection.createStatement();

                order.setId(ORDER);
                ResultSet max_object_id = statement.executeQuery("SELECT MAX(object_id) from objects");
                if (max_object_id.next()) {
                    int OBJECT_ID = (max_object_id.getInt("max") + 1);

                    statement.executeUpdate("INSERT into objects (object_id, object_type_id, name) VALUES ( default, \'"
                            + order.getId() + "\' , \'" + "order" + "\'" + "\'" + OBJECT_ID + "\'" + ")");

                    ResultSet findId = statement.executeQuery("SELECT object_id from objects where object_type_id = \'"
                            + order.getId() + "\' and name =  \'" + "order" + "\'" + "\'" + OBJECT_ID + "\'");

                    if(findId.next()){
                        order.setId(findId.getInt("object_id"));
                    }

                    String SQLtimestamp = "INSERT INTO params (attribute_id, object_id, timestamp_value) VALUES (?, ?, ?)";
                    String SQLmoney = "INSERT INTO params (attribute_id, object_id, money_value) VALUES (?, ?, ?)";
                    String SQLref = "INSERT INTO table_references (attribute_id, object_id, reference_id) VALUES (?, ?, ?)";

                    PreparedStatement statement1 = connection.prepareStatement(SQLtimestamp);

                    statement1.setInt(1, BID_DATE);
                    statement1.setInt(2, order.getId());
                    statement1.setTimestamp(3, order.getBidDate());

                    statement1.executeUpdate();

                    statement1 = connection.prepareStatement(SQLmoney);

                    statement1.setInt(1, CUSTOMER_PRICE);
                    statement1.setInt(2, order.getId());
                    statement1.setBigDecimal(3, BigDecimal.valueOf(order.getCustomerPrice()));

                    statement1.executeUpdate();

                    statement1 = connection.prepareStatement(SQLref);

                    statement1.setInt(1, REFERENCE_TO_AUCTION);
                    statement1.setInt(2, order.getId());
                    statement1.setInt(3, idAuction);

                    statement1.addBatch();

                    statement1.setInt(1, REFERENCE_TO_CUSTOMER_WHO_PARTICIPATES_IN_AUCTION);
                    statement1.setInt(2, order.getId());
                    statement1.setInt(3, idCustomer);

                    statement1.addBatch();

                    statement1.executeBatch();

                    System.out.println("заказ успешно создан");
                    connection.commit();
                }
            }
            catch (SQLException throwables) {
                throwables.printStackTrace();
                try {
                    connection.rollback(savepointOne);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }
        else if(auctionDAO.auctionIsCompleted(idAuction, connection)){
            auctionDAO.auctionStatus( idAuction, "Completed", connection);

            Statement statement = null;
            statement = connection.createStatement();

            ResultSet resultSetVinner = statement.executeQuery("SELECT reference_id FROM " +
                    "(SELECT * FROM (SELECT DISTINCT ON (\"money_value\") * FROM " +
                    "  (SELECT * FROM (SELECT tew.object_id, tew.attribute_id, reference_id, money_value FROM\n" +
                    "    (SELECT * FROM (SELECT * FROM objects NATURAL JOIN table_references)\n" +
                    "        as rew where object_type_id=\'" + ORDER + "\' " +
                    "          and attribute_id=\'" + REFERENCE_TO_CUSTOMER_WHO_PARTICIPATES_IN_AUCTION + "\')\n" +
                    "            as tew JOIN params on tew.object_id = params.object_id)\n" +
                    "                as mef WHERE money_value notnull)\n" +
                    "                    as pef ORDER BY \"money_value\" DESC)\n" +
                    "                        as pog LIMIT 1 OFFSET 0) as kew;");

            if(resultSetVinner.next()){
                int vinnerId = resultSetVinner.getInt("reference_id");

                Customer vinner = customerDAO.getCustomer(vinnerId);
                connection.close();
                return "Аукцион завершен! Победитель: " + vinner.toString();
            }

        }
        else {
            connection.close();
            return "Аукцион отменен или не существует.";
        }
        connection.close();
        return "Заявка на участие создана" + order.toString();
    }



    public Order getBid(int bidId) throws SQLException {

        Connection connection = this.sqLrequests.getConnection();

        Order order = new Order();
        order.setId(bidId);

        String SQLdouble = "select money_value from params WHERE object_id = ? " +
                "and attribute_id = ? ";
        String SQLTimestamp = "select timestamp_value from params WHERE object_id = ? " +
                "and attribute_id = ? ";

        PreparedStatement statement1 = connection.prepareStatement(SQLdouble);
        PreparedStatement statement2 = connection.prepareStatement(SQLTimestamp);

        statement1.setInt(1, bidId);
        statement1.setInt(2, CUSTOMER_PRICE);

        statement2.setInt(1, bidId);
        statement2.setInt(2, BID_DATE);

        ResultSet resultSet1 = statement1.executeQuery();
        ResultSet resultSet2 = statement2.executeQuery();

        if (resultSet1.next() & resultSet2.next()){
            order.setCustomerPrice(resultSet1.getDouble("money_value"));
            order.setBidDate(resultSet2.getTimestamp("timestamp_value"));
        }
        connection.close();
        return order;
    }


    public List<Order> getAllBid(int auctionId) throws SQLException {

        Connection connection = this.sqLrequests.getConnection();

        List<Order> orders = new ArrayList<>();

        Statement statement1 = connection.createStatement();

        ResultSet allBid = statement1.executeQuery(
                "SELECT Count(*) from table_references WHERE " +
                        "attribute_id = \'" + REFERENCE_TO_AUCTION + "\' and reference_id = \'" + auctionId + "\'");

        if (allBid.next()) {
            int[] count = new int[allBid.getInt("count")];

            ResultSet bidId = statement1.executeQuery(
                    "SELECT object_id from table_references WHERE " +
                            "attribute_id = \'" + REFERENCE_TO_AUCTION + "\' and reference_id = \'" + auctionId + "\'");

            int i = 0;
            while (bidId.next()) {
                count[i] = bidId.getInt("object_id");
                i++;
            }

            for (int j = 0; j < count.length; j++) {
                orders.add(getBid(count[j]));
            }
        }

        connection.close();
        return orders;
    }
}
