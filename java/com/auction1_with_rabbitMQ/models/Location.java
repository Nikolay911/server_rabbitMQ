package com.auction1_with_rabbitMQ.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Location {

    private int id;
    private String city;
    private String street;
    private String home;
    private int apartment;
    private String postCode;

}
