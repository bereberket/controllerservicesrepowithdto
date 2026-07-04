package com.example.bankservice.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
@Setter
@Getter


public class RegisterRequestDto {
    @NotBlank(message = "Name shouldn't be null")
    private String username;

    @NotBlank(message = "Password shouldn't be null")
    private String password;


}

