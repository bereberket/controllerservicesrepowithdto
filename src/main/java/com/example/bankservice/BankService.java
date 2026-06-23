package com.example.bankservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.management.RuntimeMBeanException;

@Service
public class BankService {

    private static final Logger log = LoggerFactory.getLogger(BankService.class);

    private final BankRepo bankreposi;

    public BankService(BankRepo bankreposi) {
        this.bankreposi = bankreposi;
    }

    public BankAccountResponseDto withdraw(String accountNumber, double amount) {
        log.info("Withdraw operation started. Account Number: {}, Amount of Withdraw: {}", accountNumber, amount);
        if(amount<=0){
            log.warn("Invalid amount. Please enter valid numbers !");
            throw new RuntimeException("Enter valid number");
        }
        BankAccount account = bankreposi.findByAccountNumber(accountNumber)
                .orElseThrow(() -> {
                    log.warn("Error. Account doesn't exist. Account No: {}", accountNumber);
                     return new AccountNotFoundException("There isn't any account");

                });

        BankAccountResponseDto dto = new BankAccountResponseDto();
        if(account.getBalance()< amount){
            log.warn("Unsufficient balance!");
            String InfoMessage = String.format("Your balance is insufficient for this. You should deposit %.2f TL for this operation. Account Number: %s, Current Balance: %.2f TL",
            amount-account.getBalance(),accountNumber,account.getBalance()
            );
            throw new InsufficientBalanceException(InfoMessage);
        }
        double newBalance = account.getBalance() - amount;
        account.setBalance(newBalance);
        bankreposi.save(account);
        log.info("Your operation is successful. Account Number: {}, Current Balance: {}", accountNumber, account.getBalance());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setBalance(account.getBalance());
        dto.setName(account.getName());
        return dto;
    }
    public BankAccountResponseDto deposit(String accountNumber, double depositAmuont){
        log.info("Deposit operation started. Account Number: {}, Amount of Deposit: {}", accountNumber, depositAmuont);
        if(depositAmuont<=0){
            log.warn("Zero or smaller number! ");
            throw new RuntimeException("Enter valid number");
        }
        BankAccount account = bankreposi.findByAccountNumber(accountNumber).orElseThrow(() -> new AccountNotFoundException("There isn't any account like that"));
        BankAccountResponseDto dto = new BankAccountResponseDto();
        double newBalanceDepo = account.getBalance() + depositAmuont;
        account.setBalance(newBalanceDepo);
        bankreposi.save(account);
        log.info("Deposit operation is successful. Account Number : {}, Current Balance: {} ", accountNumber, account.getBalance());
        dto.setBalance(account.getBalance());
        dto.setName(account.getName());
        dto.setAccountNumber(account.getAccountNumber());

        return dto;
    }

    public BankAccountResponseDto createAccount(String name, String accountNumber){
        BankAccount bankAccount = new BankAccount();
        BankAccountResponseDto dto = new BankAccountResponseDto();
        bankAccount.setBalance(0.0);
        if(name == null || name.isEmpty()){
            log.warn("Nothing was entered");
            throw new IllegalArgumentException("Name shouldn't be null ");
        }
        bankAccount.setName(name);
        if(accountNumber == null || accountNumber.isEmpty()){
            log.warn("Nothing was entered");
            throw new IllegalArgumentException("It shouldn't be empty");
        }
        bankAccount.setAccountNumber(accountNumber);
        bankreposi.save(bankAccount);
        log.info("Account created successfully. Account Number: {}, Balance: {}", accountNumber, bankAccount.getBalance());
        dto.setName(bankAccount.getName());
        dto.setAccountNumber(bankAccount.getAccountNumber());
        dto.setBalance(bankAccount.getBalance());
        return dto;
    }
    


}