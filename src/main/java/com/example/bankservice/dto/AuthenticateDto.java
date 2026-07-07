package com.example.bankservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticateDto {
    private final String message;
    private final String username;

    public AuthenticateDto(String message, String username){
        this.message = message;
        this.username = username;
    }


}
