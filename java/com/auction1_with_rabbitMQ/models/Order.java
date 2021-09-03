package com.auction1_with_rabbitMQ.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    private int id;
    private double customerPrice;
    private Timestamp bidDate;

}
