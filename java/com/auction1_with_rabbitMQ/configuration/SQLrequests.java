package com.auction1_with_rabbitMQ.configuration;

import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class SQLrequests {

    public ConfigProperties configProperties;

    public SQLrequests(ConfigProperties param) {
        configProperties = param;
    }


    public Connection getConnection() {
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(configProperties.getURL(), configProperties.getUSERNAME(), configProperties.getPASSWORD());
            connection.setAutoCommit(false);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return connection;
    }
}
