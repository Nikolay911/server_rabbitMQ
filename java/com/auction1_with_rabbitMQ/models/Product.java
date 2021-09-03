package com.auction1_with_rabbitMQ.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    private int id;
    @JsonIgnore
    private File foto;
    private String path;
    private String productDescription;
    private double startPrice;

}
