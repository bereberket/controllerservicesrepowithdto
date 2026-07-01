package com.example.bankservice.dto;
import jakarta.validation.constraints.NotBlank;



public class CreateAccountRequestDto {
    @NotBlank(message = "Name shouldn't be null")
    private String name;

    @NotBlank(message = "Account Number shouldn't be null")
    private String accountNumber;

    public String getName() { return name; }
    public String getAccountNumber() { return accountNumber; }
    public void setName(String name) { this.name = name; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
}
