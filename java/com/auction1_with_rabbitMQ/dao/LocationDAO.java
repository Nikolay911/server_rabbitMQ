package com.auction1_with_rabbitMQ.dao;

import com.auction1_with_rabbitMQ.models.Location;
import com.auction1_with_rabbitMQ.configuration.SQLrequests;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.auction1_with_rabbitMQ.consts.AttributesConsts.*;
import static com.auction1_with_rabbitMQ.consts.TypeConsts.LOCATION;

@Component
public class LocationDAO {

    private final SQLrequests sqLrequests;

    public LocationDAO(SQLrequests sqLrequests) {
        this.sqLrequests = sqLrequests;
    }


    public Location createLocation(String city, String street, String home, int apartment, String postcode, Connection connection) {

        Location location = new Location();

        location.setCity(city);
        location.setStreet(street);
        location.setHome(home);
        location.setApartment(apartment);
        location.setPostCode(postcode);

        try {
            Statement statement = connection.createStatement();

            location.setId(LOCATION);
            ResultSet max_object_id = statement.executeQuery("SELECT MAX(object_id) from objects");

            if (max_object_id.next()) {
                int OBJECT_ID = (max_object_id.getInt("max") + 1);
                statement.executeUpdate("INSERT into objects (object_id, object_type_id, name) VALUES ( default, \'" +
                        location.getId() + "\' , \'" + "location" + "\'" + "\'" + OBJECT_ID + "\'" + ")");

                ResultSet findId = statement.executeQuery("SELECT object_id from objects where object_type_id = \'" +
                        location.getId() + "\' and name =  \'" + "location" + "\'" + "\'" + OBJECT_ID + "\'");

                if (findId.next()) {
                    location.setId(findId.getInt("object_id"));
                }

                String SQLstring = "INSERT into params" +
                        " (attribute_id, object_id, string_value) VALUES ( ?, ?, ?)";
                String SQLint = "INSERT into params" +
                        " (attribute_id, object_id, int_value) VALUES ( ?, ?, ?)";

                PreparedStatement statement1 = connection.prepareStatement(SQLstring);

                statement1.setInt(1, CITY);
                statement1.setInt(2, location.getId());
                statement1.setString(3, city);

                statement1.addBatch();

                statement1.setInt(1, STREET);
                statement1.setInt(2, location.getId());
                statement1.setString(3, street);

                statement1.addBatch();

                statement1.setInt(1, HOME);
                statement1.setInt(2, location.getId());
                statement1.setString(3, home);

                statement1.addBatch();

                statement1.setInt(1, POSTCODE);
                statement1.setInt(2, location.getId());
                statement1.setString(3, postcode);

                statement1.addBatch();

                statement1.executeBatch();

                statement1 = connection.prepareStatement(SQLint);

                statement1.setInt(1, APARTMENT);
                statement1.setInt(2, location.getId());
                statement1.setInt(3, apartment);

                statement1.executeUpdate();

            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return location;
    }


    public Location getLocation(int id) throws SQLException {

        Connection connection = this.sqLrequests.getConnection();

        Location location = new Location();
        location.setId(id);

        try {

            String SQLstring = "select string_value from params WHERE object_id = ? " +
                    "and attribute_id = ? ";
            String SQLint = "select int_value from params WHERE object_id = ? " +
                    "and attribute_id = ? ";

            PreparedStatement statement1 = connection.prepareStatement(SQLstring);
            PreparedStatement statement2 = connection.prepareStatement(SQLstring);
            PreparedStatement statement3 = connection.prepareStatement(SQLstring);
            PreparedStatement statement4 = connection.prepareStatement(SQLint);
            PreparedStatement statement5 = connection.prepareStatement(SQLstring);

            statement1.setInt(1, id);
            statement1.setInt(2, CITY);

            statement2.setInt(1, id);
            statement2.setInt(2, STREET);

            statement3.setInt(1, id);
            statement3.setInt(2, HOME);

            statement4.setInt(1, id);
            statement4.setInt(2, APARTMENT);

            statement5.setInt(1, id);
            statement5.setInt(2, POSTCODE);

            ResultSet resultSet1 = statement1.executeQuery();
            ResultSet resultSet2 = statement2.executeQuery();
            ResultSet resultSet3 = statement3.executeQuery();
            ResultSet resultSet4 = statement4.executeQuery();
            ResultSet resultSet5 = statement5.executeQuery();

            if (resultSet1.next() & resultSet2.next() & resultSet3.next() & resultSet4.next() & resultSet5.next()) {
                location.setCity(resultSet1.getString("string_value"));
                location.setStreet(resultSet2.getString("string_value"));
                location.setHome(resultSet3.getString("string_value"));
                location.setApartment(resultSet4.getInt("int_value"));
                location.setPostCode(resultSet5.getString("string_value"));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        connection.close();
        return location;
    }

    public List<Location> getAllLocationToCustomer(int idCustomer) throws SQLException {

        Connection connection = this.sqLrequests.getConnection();

        List<Location> locations = new ArrayList<>();

        String countLocToCustomer = "SELECT Count(*) from table_references WHERE attribute_id = ? and object_id = ?";
        String idLocToCustomer = "SELECT reference_id from table_references WHERE attribute_id = ? and object_id = ?";

        PreparedStatement statement = connection.prepareStatement(countLocToCustomer);
        statement.setInt(1, REFERENCE_TO_LOCATION);
        statement.setInt(2, idCustomer);
        ResultSet resultSet = statement.executeQuery();

        statement = connection.prepareStatement(idLocToCustomer);
        statement.setInt(1, REFERENCE_TO_LOCATION);
        statement.setInt(2, idCustomer);
        ResultSet resultSet1 = statement.executeQuery();

        if (resultSet.next()) {
            int[] count = new int[resultSet.getInt("count")];

            int i = 0;
            while (resultSet1.next()) {
                count[i] = resultSet1.getInt("reference_id");
                i++;
            }

            for (int j = 0; j < count.length; j++) {
                locations.add(getLocation(count[j]));
            }
        }
        return locations;
    }


    public boolean addLocationToCustomer(int idObjectCustomer, JsonNode body) throws SQLException {

        Connection connection = this.sqLrequests.getConnection();

        ObjectMapper objectMapper = new ObjectMapper();
        Location objlocation = objectMapper.convertValue(body, Location.class);

        Savepoint savepointOne = null;
        try {
            savepointOne = connection.setSavepoint("SavepointOne");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        Location addLocation = createLocation(objlocation.getCity(), objlocation.getStreet(), objlocation.getHome(),
                objlocation.getApartment(), objlocation.getPostCode(), connection);
        int idObjectLocation = addLocation.getId();

        try {
            String SQLreq = "INSERT into table_references values (? , ?, ?)";

            PreparedStatement statement1 = connection.prepareStatement(SQLreq);

            statement1.setInt(1, REFERENCE_TO_LOCATION);
            statement1.setInt(2, idObjectCustomer);
            statement1.setInt(3, idObjectLocation);

            statement1.executeUpdate();

            connection.commit();
            connection.close();
            return true;

        } catch (SQLException throwables) {
            try {
                connection.rollback(savepointOne);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            throwables.printStackTrace();
        }
        connection.close();
        return false;
    }
}
