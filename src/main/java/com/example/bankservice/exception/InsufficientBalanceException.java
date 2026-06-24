package com.example.bankservice;

public class InsufficientBalanceException extends RuntimeException{
    public InsufficientBalanceException(String message){
        super(message);
    }
}


//List<Integer> numbers  = numbers.stream().map(n -> n*2 ).collect(Collectors.toList());
// List<String> numbers  = numbers.stream().map(n -> n + " TL").collect(Collectors.toList());
// List<Integer> numbers = numbers.stream().filter(n-> n %2 == 0).collect(Collectors.toList());


// List<BankAccount> accounts = accounts.stream()
//        .filter(accounts -> accounts.getBalance() > 1000)
//        .map(accounts ->  accounts.getName())
//        .collect(Collectors.toList());

/*
String result =
        names.stream()
             .map(name -> name.toUpperCase())
             .collect(Collectors.joining("- "))
 */