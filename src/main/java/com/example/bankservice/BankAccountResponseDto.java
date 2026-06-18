package com.example.bankservice;



public class BankAccountResponseDto {

    private String name;

    private double balance;

    private String accountNumber;



    public String getName(){

        return name;

    }

    public double getBalance(){

        return balance;

    }

    public String getAccountNumber(){

        return accountNumber;

    }

    public void setBalance(double balance){

        this.balance = balance;

    }

    public void setName(String name){

        this.name = name;

    }

    public void setAccountNumber(String accountNumber){

        this.accountNumber = accountNumber;

    }

}