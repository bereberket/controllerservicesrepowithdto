package com.example.bankservice.dto;

public class CreateAccountRequestDto {

    private String name;
    private String accountNumber;

    public String getName() { return name; }
    public String getAccountNumber() { return accountNumber; }
    public void setName(String name) { this.name = name; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
}