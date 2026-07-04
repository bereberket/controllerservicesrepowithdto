package com.example.bankservice.dto;

public class AuthenticateDto {
    public String message;
    public String username;

    public AuthenticateDto(String message, String username){
        this.message = message;
        this.username = username;
    }

    public String getMessage() {
        return message;
    }
    public String getUsername(){
        return username;
    }
}
