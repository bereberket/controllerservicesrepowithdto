package com.example.bankservice.service;

import com.example.bankservice.dto.BankAccountResponseDto;
import com.example.bankservice.entity.BankAccount;
import com.example.bankservice.exception.AccountAlreadyExistsException;
import com.example.bankservice.exception.AccountNotFoundException;
import com.example.bankservice.exception.InsufficientBalanceException;
import com.example.bankservice.exception.InvalidAmountException;
import com.example.bankservice.repository.BankRepo;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BankService {

    private static final Logger log = LoggerFactory.getLogger(BankService.class);

    private final BankRepo bankreposi;

    public BankService(BankRepo bankreposi) {
        this.bankreposi = bankreposi;
    }            //burada da constructor injeciton var.
    private BankAccountResponseDto convertToDto(BankAccount account) {
        BankAccountResponseDto dto = new BankAccountResponseDto();
        dto.setName(account.getName());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setBalance(account.getBalance());
        return dto;
    }
    @Transactional
    public BankAccountResponseDto withdraw(String accountNumber, double amount) {
        log.info("Withdraw operation started. Account Number: {}, Amount of Withdraw: {}", accountNumber, amount);
        if(amount<=0){
            log.warn("Invalid amount. Please enter valid numbers !");
            throw new InvalidAmountException("Enter valid number");
        }
        BankAccount account = bankreposi.findByAccountNumber(accountNumber)
                .orElseThrow(() -> {
                    log.warn("Error. Account doesn't exist. Account No: {}", accountNumber);
                     return new AccountNotFoundException("There isn't any account");

                });

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
        return  convertToDto(account);
    }
    @Transactional
    public BankAccountResponseDto deposit(String accountNumber, double depositAmount){
        log.info("Deposit operation started. Account Number: {}, Amount of Deposit: {}", accountNumber, depositAmount);
        if(depositAmount<=0){
            log.warn("Zero or smaller number! ");
            throw new InvalidAmountException("Amount must be greater than zero");
        }
        BankAccount account = bankreposi.findByAccountNumber(accountNumber).orElseThrow(() -> new AccountNotFoundException("There isn't any account like that"));
        double newBalanceDepo = account.getBalance() + depositAmount;
        account.setBalance(newBalanceDepo);
        bankreposi.save(account);
        log.info("Deposit operation is successful. Account Number : {}, Current Balance: {} ", accountNumber, account.getBalance());
        return convertToDto(account);
    }
    @Transactional
    public BankAccountResponseDto createAccount(String name, String accountNumber){
        BankAccount bankAccount = new BankAccount();
        bankAccount.setBalance(0.0);
        if(name == null || name.isEmpty()){
            log.warn("Nothing was entered");
            throw new IllegalArgumentException("Name shouldn't be null ");
        }
        bankAccount.setName(name);
        if(accountNumber == null || accountNumber.isBlank()){
            log.warn("Nothing was entered");
            throw new IllegalArgumentException("It shouldn't be empty");
        }
        if(bankreposi.findByAccountNumber(accountNumber).isPresent()){
            log.warn("Account Number exists !");
            throw new AccountAlreadyExistsException("This accountNumber exists on system. ");
        }
        bankAccount.setAccountNumber(accountNumber);
        bankreposi.save(bankAccount);
        log.info("Account created successfully. Account Number: {}, Balance: {}", accountNumber, bankAccount.getBalance());
        return convertToDto(bankAccount);
    }
    @Transactional(readOnly = true)
    public BankAccountResponseDto getAccount(String accountNumber){
        BankAccount bankAccount = bankreposi.findByAccountNumber(accountNumber).orElseThrow(() -> {
        log.warn("Account doesn't exist");
        return new AccountNotFoundException("Account doesn't exist");
        });
        return convertToDto(bankAccount);
    }


    @Transactional
    public void deleteAccount(String accountNumber){
        BankAccount bankAccount = bankreposi.findByAccountNumber(accountNumber)
                .orElseThrow(() -> {
                    log.warn("Account doesn't exist. Account Number: {}", accountNumber);
                    return new AccountNotFoundException("Account doesn't exist");
                });

        bankreposi.delete(bankAccount);
    }
    @Transactional(readOnly = true)
    public BankAccountResponseDto getAllAccounts(String accountNumber){
        BankAccount bankAccount = (BankAccount) bankreposi.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());


        return null;
    }


    public List<BankAccountResponseDto> getAllAccounts() {
    return null;
    }

    @Transactional(readOnly = true)

    public List<BankAccountResponseDto> getAccountsWithBalanceGreaterThan(double minBalance) {
        List<BankAccountResponseDto> account = bankreposi.findByBalanceGreaterThan(minBalance)
                .stream().map(this::convertToDto).toList();
        return null;
    }


}