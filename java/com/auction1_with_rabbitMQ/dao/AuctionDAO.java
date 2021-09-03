package com.auction1_with_rabbitMQ.dao;

import com.auction1_with_rabbitMQ.configuration.SQLrequests;
import com.auction1_with_rabbitMQ.models.AuctionModel;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.auction1_with_rabbitMQ.consts.AttributesConsts.*;
import static com.auction1_with_rabbitMQ.consts.TypeConsts.AUCTION;

@Component
public class AuctionDAO {

    private final SQLrequests sqLrequests;

    public AuctionDAO(SQLrequests sqLrequests) {
        this.sqLrequests = sqLrequests;
    }


    public AuctionModel createAuction(int idCustomer, int idProduct, JsonNode body) throws Exception {

        Connection connection = this.sqLrequests.getConnection();

        AuctionModel auction = new AuctionModel();

        String str = String.valueOf(body.get("auction_completion_date"));
        str = str.substring(1);
        str = str.substring(0, str.length() - 1);

        auction.setCustomerWhoCreatedAuction(idCustomer);
        auction.setAuctionCompletionDate(Timestamp.valueOf(str));

        Timestamp time = new Timestamp(System.currentTimeMillis());
        if(auction.getAuctionCompletionDate().getTime() < time.getTime()){
            throw new Exception("Время завершения аукциона не может быть меньше или равно времении создания.");
        }


        Savepoint savepointOne = null;
        try {
            savepointOne = connection.setSavepoint("SavepointOne");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        try {
            Statement statement = connection.createStatement();

            String SQLcheckUserProductExist = "SELECT object_id from table_references where attribute_id =? and reference_id = ?";
            PreparedStatement statement1 = connection.prepareStatement(SQLcheckUserProductExist);

            statement1.setInt(1, REFERENCE_TO_CUSTOMER_WHO_ADD_PRODUCT);
            statement1.setInt(2, idCustomer);

            ResultSet checkUserProductExist = statement1.executeQuery();
            while (checkUserProductExist.next()) {
                if (checkUserProductExist.getInt("object_id") == idProduct) {

                    auction.setId(AUCTION);
                    ResultSet max_object_id = statement.executeQuery("SELECT MAX(object_id) from objects");
                    if (max_object_id.next()) {
                        int OBJECT_ID = (max_object_id.getInt("max") + 1);
                        statement.executeUpdate("INSERT into objects (object_id, object_type_id, name) VALUES ( default, \'" + auction.getId() + "\' , \'" + "auction" + "\'" + "\'" + OBJECT_ID + "\'" + ")");

                        ResultSet findId = statement.executeQuery("SELECT object_id from objects where object_type_id = \'" + auction.getId() + "\' and name =  \'" + "auction" + "\'" + "\'" + OBJECT_ID + "\'");
                        if (findId.next()) {
                            auction.setId(findId.getInt("object_id"));
                        }
                        String status = "InProgress";
                        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
                        auction.setAuctionStartDate(currentTime);
                        auction.setApplicationStatus(status);

                        String SQLstring = "INSERT into params (attribute_id, object_id, string_value) VALUES (?, ?, ?)";
                        String SQLtimestamp = "INSERT into params (attribute_id, object_id, timestamp_value) VALUES (?, ?, ?)";
                        String SQLint = "INSERT into table_references values (?, ?, ?)";

                        PreparedStatement statement2 = connection.prepareStatement(SQLstring);

                        statement2.setInt(1, APPLICATION_STATUS);
                        statement2.setInt(2, auction.getId());
                        statement2.setString(3, status);

                        statement2.executeUpdate();

                        statement2 = connection.prepareStatement(SQLtimestamp);

                        statement2.setInt(1, AUCTION_START_DATE);
                        statement2.setInt(2, auction.getId());
                        statement2.setTimestamp(3, currentTime);

                        statement2.addBatch();

                        statement2.setInt(1, AUCTION_COMPLETION_DATE);
                        statement2.setInt(2, auction.getId());
                        statement2.setTimestamp(3, auction.getAuctionCompletionDate());

                        statement2.addBatch();

                        statement2.executeBatch();

                        statement2 = connection.prepareStatement(SQLint);

                        statement2.setInt(1, REFERENCE_TO_PRODUCT_WHO_PARTICIPATES_IN_AUCTION);
                        statement2.setInt(2, auction.getId());
                        statement2.setInt(3, idProduct);

                        statement2.addBatch();

                        statement2.setInt(1, REFERENCE_TO_CUSTOMER_WHO_CREATED_AUCTION);
                        statement2.setInt(2, auction.getId());
                        statement2.setInt(3, idCustomer);

                        statement2.addBatch();

                        statement2.executeBatch();

                        connection.commit();
                    }
                }
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
        return auction;
    }


    public String auctionStatus(int idAuction, String status, Connection connection) {
        try {
            String SQL = "UPDATE params Set string_value = ? where object_id = ? and string_value = ?";
            PreparedStatement statement2 = connection.prepareStatement(SQL);

            statement2.setString(1, status);
            statement2.setInt(2, idAuction);
            statement2.setString(3, "InProgress");

            statement2.executeUpdate();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return "Теперь статус аукциона с ID " + idAuction + " равен " + status;
    }


    public boolean auctionIsExist(int idAuction, Connection connection) {

        try {
            String SQL = "SELECT object_id FROM objects where object_id = ?";

            PreparedStatement statement = connection.prepareStatement(SQL);

            statement.setInt(1, idAuction);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }


    public boolean auctionIsInProgress(int idAuction, Connection connection) {

        try {
            String SQL = "SELECT string_value FROM params where object_id = ? and string_value = ?";

            PreparedStatement statement = connection.prepareStatement(SQL);

            statement.setInt(1, idAuction);
            statement.setString(2, "InProgress");

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return true;
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }


    public boolean auctionIsCompleted(int idAuction, Connection connection) {

        try {
            String SQL = "SELECT timestamp_value from params WHERE object_id = ? AND attribute_id = ?";

            PreparedStatement statement = connection.prepareStatement(SQL);

            statement.setInt(1, idAuction);
            statement.setInt(2, AUCTION_COMPLETION_DATE);

            ResultSet resultSet2 = statement.executeQuery();

            if (resultSet2.next()) {
                Timestamp currentTime = new Timestamp(System.currentTimeMillis());

                Timestamp endTime = resultSet2.getTimestamp("timestamp_value");

                if (endTime.getTime() - currentTime.getTime() <= 0) {
                    return true;
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public AuctionModel getAuction(int auctionId) throws SQLException {

        Connection connection = this.sqLrequests.getConnection();

        AuctionModel auctionModel = new AuctionModel();
        auctionModel.setId(auctionId);


        String SQLint = "select reference_id from table_references WHERE object_id = ? " +
                "and attribute_id = ? ";
        String SQLTimestamp = "select timestamp_value from params WHERE object_id = ? " +
                "and attribute_id = ? ";
        String SQLstring = "select string_value from params WHERE object_id = ? " +
                "and attribute_id = ? ";

        PreparedStatement statement1 = connection.prepareStatement(SQLint);
        PreparedStatement statement2 = connection.prepareStatement(SQLTimestamp);
        PreparedStatement statement3 = connection.prepareStatement(SQLTimestamp);
        PreparedStatement statement4 = connection.prepareStatement(SQLstring);

        statement1.setInt(1, auctionId);
        statement1.setInt(2, REFERENCE_TO_CUSTOMER_WHO_CREATED_AUCTION);

        statement2.setInt(1, auctionId);
        statement2.setInt(2, AUCTION_START_DATE);

        statement3.setInt(1, auctionId);
        statement3.setInt(2, AUCTION_COMPLETION_DATE);

        statement4.setInt(1, auctionId);
        statement4.setInt(2, APPLICATION_STATUS);

        ResultSet resultSet1 = statement1.executeQuery();
        ResultSet resultSet2 = statement2.executeQuery();
        ResultSet resultSet3 = statement3.executeQuery();
        ResultSet resultSet4 = statement4.executeQuery();

        if (resultSet1.next() & resultSet2.next() & resultSet3.next() & resultSet4.next()){
            auctionModel.setCustomerWhoCreatedAuction(resultSet1.getInt("reference_id"));
            auctionModel.setAuctionStartDate(resultSet2.getTimestamp("timestamp_value"));
            auctionModel.setAuctionCompletionDate(resultSet3.getTimestamp("timestamp_value"));
            if(auctionModel.getAuctionCompletionDate().getTime() - new Timestamp(System.currentTimeMillis()).getTime() < 0)
                auctionModel.setApplicationStatus("Completed");
            else
                auctionModel.setApplicationStatus(resultSet4.getString("string_value"));
        }
        connection.close();
        return auctionModel;
    }

    public List<AuctionModel> getAllAuctions() throws SQLException {
        Connection connection = this.sqLrequests.getConnection();

        List<AuctionModel> auctionModels = new ArrayList<>();

        Statement statement1 = connection.createStatement();

        ResultSet allCustomers = statement1.executeQuery(
                "SELECT Count(*) from objects WHERE object_type_id = \'" + AUCTION + "\'");

        if (allCustomers.next()) {
            int[] count = new int[allCustomers.getInt("count")];

            ResultSet customersId = statement1.executeQuery(
                    "SELECT object_id from objects where object_type_id = \'" + AUCTION + "\'");

            int i = 0;
            while (customersId.next()) {
                count[i] = customersId.getInt("object_id");
                i++;
            }

            for (int j = 0; j < count.length; j++) {
                auctionModels.add(getAuction(count[j]));
            }
        }

        connection.close();
        return auctionModels;
    }
}
