package com.example.bankservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransferRequestDto {
    @NotBlank(message = "ID of sender account shouldn't be null")
    private String sourceAccountNumber;

    @NotBlank(message = "ID of the beneficiary shouldn't be null")
    private String targetAccountNumber;

    @NotNull(message = "Amount shouldn't be null")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private BigDecimal amount;



}
