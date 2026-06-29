package com.example.bankservice.exception;

public class AccountAlreadyExistsException extends RuntimeException {
   public AccountAlreadyExistsException(String message){
       super(message);
   }

}
