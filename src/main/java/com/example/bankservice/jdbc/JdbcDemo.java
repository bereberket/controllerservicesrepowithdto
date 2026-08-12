package com.example.bankservice.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.math.BigDecimal;

public class JdbcDemo {

    public static void main(String[] args) {

        String url = "jdbc:h2:file:./data/bankdb";
        String username = "SA";
        String password = "";

        String sql = "SELECT *FROM BANK_ACCOUNT WHERE BALANCE > ?";

        try(
                Connection connection = java.sql.DriverManager.getConnection(url,username,password);
                PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setBigDecimal(1, new BigDecimal("5.00"));

            try(ResultSet resultSet = preparedStatement.executeQuery()){
                while(resultSet.next()){
                    String name = resultSet.getString("NAMELY");
                    String accountNumber = resultSet.getString("ACCOUNT_NUMBER");
                    BigDecimal balance = resultSet.getBigDecimal("BALANCE");

                    System.out.println(name + "-" + accountNumber + "-" + balance);

                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }


    }
}
