package com.auction1_with_rabbitMQ.dao;


import com.auction1_with_rabbitMQ.configuration.DownloadConfig;
import com.auction1_with_rabbitMQ.configuration.SQLrequests;
import com.auction1_with_rabbitMQ.models.Product;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.*;
import java.sql.*;

import static com.auction1_with_rabbitMQ.consts.AttributesConsts.*;
import static com.auction1_with_rabbitMQ.consts.TypeConsts.PRODUCT;

@Component
public class ProductDAO {

    private final SQLrequests sqLrequests;
    private final DownloadConfig downloadConfig;

    public ProductDAO(SQLrequests sqLrequests, DownloadConfig downloadConfig) {
        this.sqLrequests = sqLrequests;
        this.downloadConfig = downloadConfig;
    }


    public Product createProduct(int customerId, JsonNode body) throws SQLException {

        Connection connection = this.sqLrequests.getConnection();

        ObjectMapper objectMapper = new ObjectMapper();

        Product product = objectMapper.convertValue(body, Product.class);

        product.setProductDescription(product.getProductDescription());

        Savepoint savepointOne = null;
        try {
            savepointOne = connection.setSavepoint("SavepointOne");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        try {
            Statement statement = connection.createStatement();


            product.setId(PRODUCT);
            ResultSet max_object_id = statement.executeQuery("SELECT MAX(object_id) from objects");

            if (max_object_id.next()) {
                int OBJECT_ID = (max_object_id.getInt("max") + 1);

                statement.executeUpdate("INSERT into objects (object_id, object_type_id, name) VALUES ( default, \'"
                        + product.getId() + "\' , \'" + "product" + "\'" + "\'" + OBJECT_ID + "\'" + ")");

                ResultSet findId = statement.executeQuery("SELECT object_id from objects where object_type_id = \'"
                        + product.getId() + "\' and name =  \'" + "product" + "\'" + "\'" + OBJECT_ID + "\'");

                if (findId.next()) {
                    product.setId(findId.getInt("object_id"));
                }

                File image = new File(String.valueOf(product.getPath()));
                InputStream fis = new FileInputStream(image);
                long ilen =  image.length();

                String SQLbytea = ("INSERT into params (attribute_id, object_id, bytea_value) VALUES (?, ?, ? )");
                String SQLstring = ("INSERT into params (attribute_id, object_id, string_value) VALUES (?, ?, ? )");
                String SQLmoney = ("INSERT into params (attribute_id, object_id, money_value) VALUES (?, ?, ? )");
                String SQLint = ("INSERT into table_references  VALUES (?, ?, ? )");

                PreparedStatement stmt = connection.prepareStatement(SQLbytea);
                stmt.setInt(1, FOTO);
                stmt.setInt(2, product.getId());
                stmt.setBinaryStream(3, fis, ilen);

                stmt.execute();

                stmt = connection.prepareStatement(SQLstring);

                stmt.setInt(1, PRODUCT_DESCRIPTION);
                stmt.setInt(2, product.getId());
                stmt.setString(3, product.getProductDescription());

                stmt.execute();

                stmt = connection.prepareStatement(SQLmoney);

                stmt.setInt(1, START_PRICE);
                stmt.setInt(2, product.getId());
                stmt.setDouble(3, product.getStartPrice());

                stmt.execute();

                stmt = connection.prepareStatement(SQLint);

                stmt.setInt(1, REFERENCE_TO_CUSTOMER_WHO_ADD_PRODUCT);
                stmt.setInt(2, product.getId());
                stmt.setInt(3, customerId);

                stmt.execute();

                product.setFoto(image);
                connection.commit();
            }
        } catch (SQLException | FileNotFoundException throwables) {
            throwables.printStackTrace();
            try {
                connection.rollback(savepointOne);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        connection.close();
        return product;
    }


    public Product getProduct(int idProduct) throws SQLException {

        Connection connection = this.sqLrequests.getConnection();

        Product product = new Product();
        product.setId(idProduct);

        try {

            String SQLbytea = "select bytea_value from params WHERE object_id = ? and attribute_id = ?";
            String SQLstring = "select string_value from params WHERE object_id = ? and attribute_id = ?";
            String SQLmoney = "select money_value from params WHERE object_id = ? and attribute_id = ?";

            PreparedStatement statement1 = connection.prepareStatement(SQLbytea);
            PreparedStatement statement2 = connection.prepareStatement(SQLstring);
            PreparedStatement statement3 = connection.prepareStatement(SQLmoney);

            statement1.setInt(1, idProduct);
            statement1.setInt(2, FOTO);

            statement2.setInt(1, idProduct);
            statement2.setInt(2, PRODUCT_DESCRIPTION);

            statement3.setInt(1, idProduct);
            statement3.setInt(2, START_PRICE);

            ResultSet resultSet1 = statement1.executeQuery();
            ResultSet resultSet2 = statement2.executeQuery();
            ResultSet resultSet3 = statement3.executeQuery();

            if (resultSet1.next() & resultSet2.next() & resultSet3.next()) {

                byte[] bytes = resultSet1.getBytes("bytea_value");
                String filename = downloadConfig.getPath() + idProduct + ".png";
                FileOutputStream fos = new FileOutputStream(new File(filename));
                fos.write(bytes);
                fos.close();

                product.setPath(filename);
                product.setProductDescription(resultSet2.getString("string_value"));

                product.setStartPrice(resultSet3.getDouble("money_value"));
            }
        } catch (SQLException | IOException throwables) {
            throwables.printStackTrace();
        }
        connection.close();
        return product;
    }
}