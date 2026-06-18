package com.example.bankservice;

import org.springframework.stereotype.Service;

@Service
public class BankService {
    private final BankRepo bankreposi;

    public BankService(BankRepo bankreposi) {
        this.bankreposi = bankreposi;
    }

    public BankAccount withdraw(String accountNumber, double amount) {
        if(amount<=0){
            throw new RuntimeException("Enter valid number");
        }
        BankAccount account = bankreposi.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("There isn't any account"));


        if(account.getBalance()< amount){
            throw new RuntimeException("Unsufficient balance!");
        }
        double newBalance = account.getBalance() - amount;
        account.setBalance(newBalance);
        bankreposi.save(account);
        return account;
    }
    public BankAccount deposit(String accountNumber, double depositAmuont){
        if(depositAmuont<=0){
            throw new RuntimeException("Enter valid number");
        }
        BankAccount account = bankreposi.findByAccountNumber(accountNumber).orElseThrow(() -> new RuntimeException("There isn't any account like that"));

        double newBalanceDepo = account.getBalance() + depositAmuont;
        account.setBalance(newBalanceDepo);
        bankreposi.save(account);

        return account;
    }

    public BankAccount createAccount(String name, String AccountNumber){
        BankAccount bankAccount = new BankAccount();
        bankAccount.setName(name);
        bankAccount.setAccountNumber(AccountNumber);
        bankAccount.setBalance(0.0);
        return bankreposi.save(bankAccount);
    }
    


}