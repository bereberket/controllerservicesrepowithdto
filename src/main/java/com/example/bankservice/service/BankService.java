package com.example.bankservice.service;

import com.example.bankservice.dto.BankAccountResponseDto;
import com.example.bankservice.entity.AppUser;
import com.example.bankservice.entity.BankAccount;
import com.example.bankservice.exception.AccountAlreadyExistsException;
import com.example.bankservice.exception.AccountNotFoundException;
import com.example.bankservice.exception.InsufficientBalanceException;
import com.example.bankservice.exception.InvalidAmountException;
import com.example.bankservice.mapper.BankAccountMapper;
import com.example.bankservice.repository.AppUserRepository;
import com.example.bankservice.repository.BankRepo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BankService {

    private static final Logger log = LoggerFactory.getLogger(BankService.class);

    private final BankRepo reposition;
    private final AppUserRepository appUserRepository;


    public BankService(BankRepo reposition,AppUserRepository appUserRepository) {
        this.reposition = reposition;
        this.appUserRepository = appUserRepository;
    }            //burada da constructor injeciton var.

    @Transactional
    @CacheEvict(value = "accounts", key = "#accountNumber")
    public BankAccountResponseDto withdraw(String accountNumber, double amount) {
        log.info("Withdraw operation started. Account Number: {}, Amount of Withdraw: {}", accountNumber, amount);
        if(amount<=0){
            log.warn("Invalid amount. Please enter valid numbers !");
            throw new InvalidAmountException("Enter valid number");
        }
        BankAccount account = reposition.findByAccountNumber(accountNumber)
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
        log.info("Your operation is successful. Account Number: {}, Current Balance: {}", accountNumber, account.getBalance());
        return  BankAccountMapper.toDto(account);
    }
    @Transactional
    @CacheEvict(value = "accounts", key = "#accountNumber")
    public BankAccountResponseDto deposit(String accountNumber, double depositAmount){
        log.info("Deposit operation started. Account Number: {}, Amount of Deposit: {}", accountNumber, depositAmount);
        if(depositAmount<=0){
            log.warn("Zero or smaller number! ");
            throw new InvalidAmountException("Amount must be greater than zero");
        }
        BankAccount account = reposition.findByAccountNumber(accountNumber).orElseThrow(() -> new AccountNotFoundException("There isn't any account like that"));
        double newBalanceDepo = account.getBalance() + depositAmount;
        account.setBalance(newBalanceDepo);
        log.info("Deposit operation is successful. Account Number : {}, Current Balance: {} ", accountNumber, account.getBalance());
        return  BankAccountMapper.toDto(account);
    }
    @Transactional
    public BankAccountResponseDto createAccount(String name, String accountNumber,String username){
        AppUser appUser = appUserRepository.findByUsername(username).orElseThrow(() -> new AccountNotFoundException("User doesn't find "));
        
        BankAccount bankAccount = new BankAccount();
        bankAccount.setBalance(0.0);
        bankAccount.setName(name);

        if(reposition.findByAccountNumber(accountNumber).isPresent()){
            log.warn("Account Number exists !");
            throw new AccountAlreadyExistsException("This accountNumber exists on system. ");
        }
        bankAccount.setAccountNumber(accountNumber);
        bankAccount.setAppUser(appUser);
        reposition.save(bankAccount);
        log.info("Account created successfully. Account Number: {}, Balance: {}", accountNumber, bankAccount.getBalance());
        return  BankAccountMapper.toDto(bankAccount);
    }
    @Cacheable(value = "accounts", key = "#accountNumber")
    @Transactional(readOnly = true)
    public BankAccountResponseDto getAccount(String accountNumber){
        log.info("Account is being read from database : {}",accountNumber);

        BankAccount bankAccount = reposition.findByAccountNumber(accountNumber)
                .orElseThrow(() -> {
        log.warn("Account doesn't exist");
        return new AccountNotFoundException("Account doesn't exist");
        });
        return  BankAccountMapper.toDto(bankAccount);
    }


    @Transactional
    public void deleteAccount(String accountNumber){
        BankAccount bankAccount = reposition.findByAccountNumber(accountNumber)
                .orElseThrow(() -> {
                    log.warn("Account doesn't exist. Account Number: {}", accountNumber);
                    return new AccountNotFoundException("Account doesn't exist");
                });


        reposition.delete(bankAccount);
    }
    @Transactional(readOnly = true)
    public Page<BankAccountResponseDto> getAllAccounts(Pageable pageable){
        return reposition.findAll(pageable)
                .map(BankAccountMapper::toDto);
    }



    @Transactional(readOnly = true)

    public List<BankAccountResponseDto> getAccountsWithBalanceGreaterThan(double minBalance) {
        return reposition.findByBalanceGreaterThan(minBalance)
                .stream().map(BankAccountMapper::toDto).toList();
        
    }

    @Transactional(readOnly = true)
    public BankAccountResponseDto getAccFindByName(String name) {
        BankAccount bankAccount = reposition.findByName(name)
                .orElseThrow(() -> {
                    log.warn("Account doesn't exist. Name: {}", name);
                    return new AccountNotFoundException(
                            "Account doesn't exist. Name: " + name
                    );
                });

        return BankAccountMapper.toDto(bankAccount);
    }

}