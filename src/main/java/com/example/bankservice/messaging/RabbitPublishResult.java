package com.example.bankservice.messaging;

public record RabbitPublishResult(
        boolean successful,
        String errorMessage

){

}
