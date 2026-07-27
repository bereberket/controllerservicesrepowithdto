package com.example.bankservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter

public class BankAccountResponseDto {

    private String name;

    private BigDecimal balance;

    private String accountNumber;

}
