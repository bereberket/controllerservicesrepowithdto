package com.example.bankservice.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter


public class CreateAccountRequestDto {
    @NotBlank(message = "Name shouldn't be null")
    private String name;

    @NotBlank(message = "Account Number shouldn't be null")
    private String accountNumber;

    @NotBlank(message = "Username shouldn't be null")
    private String username;

}
