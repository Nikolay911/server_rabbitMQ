package com.auction1_with_rabbitMQ.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuctionModel {

    private int id;
    private int customerWhoCreatedAuction;
    private Timestamp auctionStartDate;
    private Timestamp auctionCompletionDate;
    private String applicationStatus;

}
