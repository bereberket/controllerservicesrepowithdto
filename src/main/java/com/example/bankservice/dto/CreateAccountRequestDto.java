package com.example.bankservice;

import org.antlr.v4.runtime.misc.NotNull;

public class CreateAccountRequestDto {
    @NotNull
    private String name;
    private String accountNumber;

    public String getName() { return name; }
    public String getAccountNumber() { return accountNumber; }
    public void setName(String name) { this.name = name; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
}