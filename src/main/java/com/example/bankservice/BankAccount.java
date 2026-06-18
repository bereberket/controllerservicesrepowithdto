package com.example.bankservice;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity

public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private long id;
    private String name;
    private double balance;
    private String AccountNumber;


    public long getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public double getBalance(){
        return balance;
    }
    public String getAccountNumber(){
        return AccountNumber;
    }
    public void setAccountNumber(String AccountNumber){
        this.AccountNumber = AccountNumber;}

    public void setName(String name){
        this.name = name;
    }
    public void setBalance(double balance){
        this.balance = balance;
    }



}
