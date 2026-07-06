package com.example.bankservice.dto;

public class AuthenticateDto {
    private final String message;
    private final String username;

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
