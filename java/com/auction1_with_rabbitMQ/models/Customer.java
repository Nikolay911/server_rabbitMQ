package com.auction1_with_rabbitMQ.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer{

    private int id;
    private String name;
    private String surname;
    private String patronymic;
    private java.sql.Date dateOfBirth;
    @JsonIgnore
    private int ReferenceToLocation;
    @JsonIgnore
    private String login;
    @JsonIgnore
    private String password;

    public Customer(int id, String name, String surname, String patronymic, java.sql.Date dateOfBirth){
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.patronymic = patronymic;
        this.dateOfBirth = dateOfBirth;

    }

    @Override
    public String toString() {
        return "{" +
                "\"id\":" + id +
                ", \"name\":" + name +
                ", \"surname\":" + surname +
                ", \"patronymic\":" + patronymic +
                ", \"dateOfBirth\":" + "\"" + dateOfBirth.toString() + "\"" +
                '}';
    }

/*    public static void main(String[] args) {
        java.sql.Date date = new Date(2013-01-06);
        Customer customer = new Customer(12, "ww", "ee", "dd", date);
        System.out.println(customer.toString());
    }*/
}
