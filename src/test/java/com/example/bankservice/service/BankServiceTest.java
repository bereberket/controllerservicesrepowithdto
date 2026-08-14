package com.example.bankservice.service;

import com.example.bankservice.dto.BankAccountResponseDto;
import com.example.bankservice.dto.CreateAccountRequestDto;
import com.example.bankservice.entity.AppUser;
import com.example.bankservice.entity.BankAccount;
import com.example.bankservice.exception.AccountAlreadyExistsException;
import com.example.bankservice.exception.AccountNotFoundException;
import com.example.bankservice.exception.InvalidAmountException;
import com.example.bankservice.enums.Role;
import com.example.bankservice.messaging.AccountCreatedEvent;
import com.example.bankservice.messaging.AccountCreatedPublisher;
import com.example.bankservice.repository.AppUserRepository;
import com.example.bankservice.repository.BankRepo;
import com.example.bankservice.repository.TransferRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.example.bankservice.enums.ActiveSituation;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
public class BankServiceTest {
    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private BankRepo bankRepo;
    @Mock
    private OutboxService outboxService;
    @Mock
    private TransferRecordRepository transferRecordRepository;

    @InjectMocks
    private BankService bankService;



    private CreateAccountRequestDto requestDto;
    private AppUser appUser;
    private static final String authenticatedUserName = "Berk";

    @BeforeEach
    void setUp(){
        requestDto = new CreateAccountRequestDto();

        requestDto.setName("Ana Hesap");

        appUser = new AppUser();
        appUser.setUsername(authenticatedUserName);
        appUser.setId(39L);

    }
    @Test
    @DisplayName("If user exists and acc. number unique then account should create.!!")
    void createAccount_shouldCreateAccount_whenUserExistsAndAccountNumberIsUnique(){


        requestDto.setAccountNumber("123");

        appUser.setUsername("Berk");

        when(appUserRepository.findByUsername(authenticatedUserName))
                .thenReturn(Optional.of(appUser));
        when(bankRepo.findByAccountNumber("TR123"))
                .thenReturn(Optional.empty());

        //ACT

        BankAccountResponseDto result =                  // result atadım çünkü assert bölümünde DTO'nun alanlarını
                bankService.createAccount(requestDto, "Berk");   // doğrulamam gerek.
        assertEquals("Ana Hesap", result.getName());
        assertEquals("TR123", result.getAccountNumber());
        assertEquals(new BigDecimal("0.00"), result.getBalance());

        ArgumentCaptor<BankAccount> accountArgumentCaptor =
                ArgumentCaptor.forClass(BankAccount.class);

        verify(bankRepo).save(accountArgumentCaptor.capture());
        BankAccount savedAccount = accountArgumentCaptor.getValue();

        assertEquals("Ana Hesap", savedAccount.getName());
        assertEquals("TR123", savedAccount.getAccountNumber());
        assertEquals(new BigDecimal("0.00"), savedAccount.getBalance());
        assertSame(appUser,savedAccount.getAppUser());  // bellekte aynı nesne mi

        // verify(bankRepo).save(any(BankAccount.class)); --- >> Parametre kontrolü yapmaz. Yanlış paramatere de kabıl eder


        //giden mesajı doğrulamak için
        ArgumentCaptor<AccountCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(AccountCreatedEvent.class);
        verify(outboxService)
                .saveAccountCreatedEvent(eventCaptor.capture());

        AccountCreatedEvent publishedEvent = eventCaptor.getValue();
        assertEquals("Ana Hesap", publishedEvent.accountName());
        assertEquals("TR123", publishedEvent.accountNumber());
        assertEquals(authenticatedUserName, publishedEvent.username());
        assertNotNull(publishedEvent.eventId());


    }

    @Test
    @DisplayName("Deposit should add decimal amount without precision loss")
    void deposit_shouldAddAmountWithoutPrecisionLoss(){
        BankAccount account = new BankAccount();
        account.setAccountNumber("TR123");
        account.setBalance(new BigDecimal("0.10"));

        when(bankRepo.findByAccountNumberAndAppUserUsername(
                "TR123",
                authenticatedUserName
        ))
                .thenReturn(Optional.of(account));

        BankAccountResponseDto result =
                bankService.deposit("123", new BigDecimal("0.20"),authenticatedUserName);

        assertEquals(new BigDecimal("0.30"), result.getBalance());
        assertEquals(new BigDecimal("0.30"), account.getBalance());

        verify(bankRepo).findByAccountNumberAndAppUserUsername(
                "TR123",
                authenticatedUserName
        );
    }

    @Test
    @DisplayName("Withdraw should subtract decimal amount without precision loss")
    void withdraw_shouldSubtractAmountWithoutPrecisionLoss(){
        BankAccount account = new BankAccount();
        account.setAccountNumber("TR123");
        account.setBalance(new BigDecimal("100.30"));

        when(bankRepo.findOwnedAccountForUpdate(
                "TR123",
                authenticatedUserName
        ))
                .thenReturn(Optional.of(account));

        BankAccountResponseDto result =
                bankService.withdraw("123", new BigDecimal("0.20"),authenticatedUserName);

        assertEquals(new BigDecimal("100.10"), result.getBalance());
        assertEquals(new BigDecimal("100.10"), account.getBalance());

        verify(bankRepo).findOwnedAccountForUpdate(
                "TR123",
                authenticatedUserName
        );
    }

    @Test
    @DisplayName("Money amount should reject more than two decimal places")
    void deposit_shouldRejectAmountWithMoreThanTwoDecimalPlaces(){
        InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> bankService.deposit(
                        "123",
                        new BigDecimal("1.001"),
                        authenticatedUserName
                )
        );

        assertEquals(
                "Amount can have at most two decimal places",
                exception.getMessage()
        );
        verify(bankRepo, never()).findByAccountNumber(anyString());
    }
    @Test
    @DisplayName("If user not exist, should throw AccountNotFoundException")
    void createAccount_shouldThrowException_whenUserDoesNotExist(){

        requestDto.setAccountNumber("149");

        when(appUserRepository.findByUsername(authenticatedUserName))
                .thenReturn(Optional.empty());

        AccountNotFoundException exception = assertThrows(
                AccountNotFoundException.class,
                () -> bankService.createAccount(requestDto, "Berk")
        );

        assertEquals("User not find", exception.getMessage());

        verify(appUserRepository)
                .findByUsername(authenticatedUserName);

        verify(bankRepo, never())
                .findByAccountNumber(anyString());

        verify(bankRepo, never())
                .save(any(BankAccount.class));


    }

    @Test
    @DisplayName("If account already exists, should throw AccountAlreadyExists")
    void createAccount_shouldThrowException_whenAccountAlreadyExists(){

        requestDto.setAccountNumber("149");

        appUser.setUsername("Berk");

        BankAccount existingAccount = new BankAccount();
        existingAccount.setAccountNumber("TR149");

        when(appUserRepository.findByUsername(authenticatedUserName))
                .thenReturn(Optional.of(appUser));

        when(bankRepo.findByAccountNumber("TR149"))
                .thenReturn(Optional.of(existingAccount));

        AccountAlreadyExistsException exception = assertThrows(
                AccountAlreadyExistsException.class,
                () -> bankService.createAccount(requestDto,"Berk")
        );

        assertEquals(
                "This account number exists",
                exception.getMessage()
        );

        verify(appUserRepository)
                .findByUsername(authenticatedUserName);

        verify(bankRepo)
                .findByAccountNumber("TR149");

        verify(bankRepo, never())
                .save(any(BankAccount.class));



    }

    @Test
    void deleteAccount_shouldDeleteOwnAccount_whenUserIsCustomer() {
        appUser.setRole(Role.CUSTOMER);

        BankAccount account = new BankAccount();
        account.setAccountNumber("TR123");
        account.setAppUser(appUser);

        when(appUserRepository.findByUsername(authenticatedUserName))
                .thenReturn(Optional.of(appUser));
        when(bankRepo.findByAccountNumberAndAppUserUsername(
                "TR123",
                authenticatedUserName
        )).thenReturn(Optional.of(account));

        bankService.deleteAccount("123", authenticatedUserName);

        verify(bankRepo).findByAccountNumberAndAppUserUsername(
                "TR123",
                authenticatedUserName
        );
        verify(bankRepo, never()).findByAccountNumber("TR123");
        verify(bankRepo).delete(account);
    }

    @Test
    void deleteAccount_shouldDeleteAnyAccount_whenUserIsAdmin() {
        AppUser admin = new AppUser();
        admin.setUsername("Admin");
        admin.setRole(Role.ADMIN);

        BankAccount account = new BankAccount();
        account.setAccountNumber("TR123");

        when(appUserRepository.findByUsername("Admin"))
                .thenReturn(Optional.of(admin));
        when(bankRepo.findByAccountNumber("TR123"))
                .thenReturn(Optional.of(account));

        bankService.deleteAccount("123", "Admin");

        verify(bankRepo).findByAccountNumber("TR123");
        verify(bankRepo, never())
                .findByAccountNumberAndAppUserUsername(
                        anyString(),
                        anyString()
                );
        verify(bankRepo).delete(account);
    }

    @Test
    void deleteAccount_shouldRejectAccountOwnedByAnotherUser_whenUserIsCustomer() {
        appUser.setRole(Role.CUSTOMER);

        when(appUserRepository.findByUsername(authenticatedUserName))
                .thenReturn(Optional.of(appUser));
        when(bankRepo.findByAccountNumberAndAppUserUsername(
                "TR123",
                authenticatedUserName
        )).thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> bankService.deleteAccount(
                        "123",
                        authenticatedUserName
                )
        );

        verify(bankRepo, never()).delete(any(BankAccount.class));
    }

    @Test
    void getMyAccounts_shouldReturnAccountsOwnedByAuthenticatedUser() {
        BankAccount firstAccount = new BankAccount();
        firstAccount.setName("Ana Hesap");
        firstAccount.setAccountNumber("TR123");
        firstAccount.setBalance(new BigDecimal("100.00"));

        BankAccount secondAccount = new BankAccount();
        secondAccount.setName("Birikim Hesabi");
        secondAccount.setAccountNumber("TR456");
        secondAccount.setBalance(new BigDecimal("250.00"));

        when(bankRepo.findByAppUserUsername(authenticatedUserName))
                .thenReturn(List.of(firstAccount, secondAccount));

        List<BankAccountResponseDto> result =
                bankService.getMyAccounts(authenticatedUserName);

        assertEquals(2, result.size());
        assertEquals("TR123", result.get(0).getAccountNumber());
        assertEquals("TR456", result.get(1).getAccountNumber());

        verify(bankRepo).findByAppUserUsername(authenticatedUserName);
    }

}
