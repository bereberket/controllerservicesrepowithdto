package com.example.bankservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {

    @NotBlank(message = "Username shouldn't be null")
    private String username;

    @NotBlank(message = "Password shouldn't be null")
    private String password;
}
