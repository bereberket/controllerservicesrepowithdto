package com.example.bankservice.service;

import com.example.bankservice.dto.BankAccountResponseDto;
import com.example.bankservice.dto.CreateAccountRequestDto;
import com.example.bankservice.entity.AppUser;
import com.example.bankservice.entity.BankAccount;
import com.example.bankservice.exception.AccountAlreadyExistsException;
import com.example.bankservice.exception.AccountNotFoundException;
import com.example.bankservice.exception.InsufficientBalanceException;
import com.example.bankservice.exception.InvalidAmountException;
import com.example.bankservice.enums.Role;
import com.example.bankservice.mapper.BankAccountMapper;
import com.example.bankservice.messaging.AccountCreatedEvent;
import com.example.bankservice.repository.AppUserRepository;
import com.example.bankservice.repository.BankRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
@Service
public class BankService {

    private static final Logger log = LoggerFactory.getLogger(BankService.class);
    private static final BigDecimal ZERO_BALANCE = new BigDecimal("0.00");

    private final BankRepo reposition;
    private final AppUserRepository appUserRepository;
    private final OutboxService outboxService;


    public BankService(BankRepo reposition, AppUserRepository appUserRepository,OutboxService  outboxService) {
        this.reposition = reposition;
        this.appUserRepository = appUserRepository;
        this.outboxService = outboxService;
    }

    private String formatAccountNumber(String accountNumber){
        if(accountNumber == null || accountNumber.isBlank()){
            throw new IllegalArgumentException(
                    "Account number shouldn't be null"
            );
        }
        String normalized =
                accountNumber.trim().toUpperCase();
        if(normalized.startsWith("TR")){
            return normalized;

        }
        return "TR" + normalized;
    }

    private BigDecimal validateMoneyAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }

        try {
            return amount.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new InvalidAmountException(
                    "Amount can have at most two decimal places"
            );
        }
    }

    @Transactional
    public BankAccountResponseDto createAccount(CreateAccountRequestDto requestDto, String authenticatedUserName){
        AppUser appUser = appUserRepository.findByUsername(authenticatedUserName).orElseThrow(() -> new AccountNotFoundException("User not find"));
        String formattedAccountNumber =
                formatAccountNumber(requestDto.getAccountNumber());

        BankAccount bankAccount = new BankAccount();
        bankAccount.setBalance(ZERO_BALANCE);
        bankAccount.setName(requestDto.getName());

        if(reposition.findByAccountNumber(formattedAccountNumber).isPresent()){
            log.warn("Account Number exists !");
            throw new AccountAlreadyExistsException("This account number exists");
        }
        bankAccount.setAccountNumber(formattedAccountNumber);
        bankAccount.setAppUser(appUser);
        reposition.save(bankAccount);

        AccountCreatedEvent event = new AccountCreatedEvent(
                UUID.randomUUID(),
                formattedAccountNumber,
                authenticatedUserName,
                bankAccount.getName(),
                Instant.now()

        );
       outboxService.saveAccountCreatedEvent(event);

        return  BankAccountMapper.toDto(bankAccount);
    }
    @Transactional
    public BankAccountResponseDto withdraw(String accountNumber, BigDecimal amount,String authenticatedUserName) {
        String formattedAccountNumber =
                formatAccountNumber(accountNumber);
        BigDecimal validatedAmount = validateMoneyAmount(amount);
        log.info("Withdraw operation started. Account Number: {}, Amount of Withdraw: {}", formattedAccountNumber, amount);

        BankAccount account = reposition.findOwnedAccountForUpdate(formattedAccountNumber,authenticatedUserName)
                .orElseThrow(()->{
                    log.warn("Account not find");
                    return new AccountNotFoundException("Account doesn't exist");
                });


        if(account.getBalance().compareTo(validatedAmount) < 0){
            log.warn("Unsufficient balance!");
            String infoMessage = String.format("Your balance is insufficient for this. You should deposit %.2f TL for this operation. Account Number: %s, Current Balance: %.2f TL",
                    validatedAmount.subtract(account.getBalance()),
                    formattedAccountNumber,
                    account.getBalance()
            );
            throw new InsufficientBalanceException(infoMessage);
        }
        BigDecimal newBalance = account.getBalance().subtract(validatedAmount);
        account.setBalance(newBalance);
        log.info("Your operation is successful. Account Number: {}, Current Balance: {}", formattedAccountNumber, account.getBalance());
        return  BankAccountMapper.toDto(account);
    }
    @Transactional
    public BankAccountResponseDto deposit(String accountNumber, BigDecimal depositAmount, String authenticatedUserName){
        String formattedAccountNumber =
                formatAccountNumber(accountNumber);
        BigDecimal validatedAmount = validateMoneyAmount(depositAmount);
        log.info("Deposit operation started. Account Number: {}, Amount of Deposit: {}",formattedAccountNumber,depositAmount);

        BankAccount account = reposition.findByAccountNumberAndAppUserUsername(formattedAccountNumber,authenticatedUserName)
                .orElseThrow(()->{
                    log.warn("Account not exist");
                    return new AccountNotFoundException("Account doesn't exist");
                });
        BigDecimal newBalanceDepo = account.getBalance().add(validatedAmount);
        account.setBalance(newBalanceDepo);
        log.info("Deposit operation is successful. Account Number : {}, Current Balance: {} ", accountNumber, account.getBalance());
        return  BankAccountMapper.toDto(account);
    }

    @Transactional(readOnly = true)
    public BankAccountResponseDto getAccount(String accountNumber,String authenticatedUserName){
        String formattedAccountNumber =
                formatAccountNumber(accountNumber);
        log.info("Account is being read from database : {}",formattedAccountNumber);

        BankAccount bankAccount = reposition.findByAccountNumberAndAppUserUsername(formattedAccountNumber,authenticatedUserName)
                .orElseThrow(()->{
                    log.warn("Account do not exist");
                    return new AccountNotFoundException("Account doesn't exist");
                });
        return  BankAccountMapper.toDto(bankAccount);
    }


    @Transactional
    public void deleteAccount(String accountNumber,String authenticatedUserName){
        String formattedAccountNumber =
                formatAccountNumber(accountNumber);

        AppUser authenticatedUser = appUserRepository
                .findByUsername(authenticatedUserName)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found")
                );

        BankAccount bankAccount;

        if (authenticatedUser.getRole() == Role.ADMIN) {
            bankAccount = reposition
                    .findByAccountNumber(formattedAccountNumber)
                    .orElseThrow(() -> {
                        log.warn("Account don't found");
                        return new AccountNotFoundException("Account doesn't exist");
                    });
        } else {
            bankAccount = reposition
                    .findByAccountNumberAndAppUserUsername(
                            formattedAccountNumber,
                            authenticatedUserName
                    )
                    .orElseThrow(() -> {
                    log.warn("Account doesn't exist");
                    return new AccountNotFoundException("Account doesn't exist");
                });
        }


        reposition.delete(bankAccount);
    }
    @Transactional(readOnly = true)
    public Page<BankAccountResponseDto> getAllAccounts(
            int page,
            int size,
            String sortBy,
            String direction
    ){
        Sort.Direction sortDirection;
        if(direction.equalsIgnoreCase("asc")){
            sortDirection = Sort.Direction.ASC;
        }else if(direction.equalsIgnoreCase("desc")){
            sortDirection = Sort.Direction.DESC;
        }else{
            throw new IllegalArgumentException("Must be ASC or DESC");
        }

        List<String> allowedSortFields = List.of("id", "name", "balance","accountNumber");

        if(!allowedSortFields.contains(sortBy)){
            throw new IllegalArgumentException("Invalid sort parameter");
        }
        if(page<0){
            throw new IllegalArgumentException("Must be positive");
        }
        if(size<1 || size>100){
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        Pageable pageable =
                PageRequest.of(page, size, sortDirection, sortBy);

        return reposition.findAll(pageable)
                .map(BankAccountMapper::toDto);
    }




    @Transactional(readOnly = true)
    public List<BankAccountResponseDto> getAccountsWithBalanceGreaterThan(BigDecimal minBalance,String authenticatedUserName) {
        return reposition.findByBalanceGreaterThanAndAppUserUsername(minBalance,authenticatedUserName)
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
    @Transactional
    public void deleteUser(String userName){

        AppUser appUser = appUserRepository.findByUsername(userName)
                .orElseThrow(() -> {
            log.warn("User not found");
            return new UsernameNotFoundException("User not find");

        });

        appUserRepository.delete(appUser);
    }

    @Transactional
    public List<BankAccountResponseDto> createAccounts(
            List<CreateAccountRequestDto> requestDtos,
            String authenticatedUserName
    ) {
        if (requestDtos == null || requestDtos.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one account request is required"
            );
        }
        return requestDtos.stream()
                .map(dto -> createAccount(dto, authenticatedUserName))
                .toList();
    }
}


