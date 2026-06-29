package com.example.bankservice.dto;

public class ErrorResponseDto {
    private final String message;
    private final int status;
    private final String error;


    public ErrorResponseDto(final String message, final int status, final String error){
        this.error = error;
        this.status = status;
        this.message = message;

    }

    public String getMessage(){
        return message;
    }
    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }
}



