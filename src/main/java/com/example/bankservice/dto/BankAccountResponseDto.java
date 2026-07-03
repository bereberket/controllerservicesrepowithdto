package com.example.bankservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class BankAccountResponseDto {

    private String name;

    private double balance;

    private String accountNumber;

}