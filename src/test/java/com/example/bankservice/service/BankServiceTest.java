package com.example.bankservice.service;

import com.example.bankservice.dto.BankAccountResponseDto;
import com.example.bankservice.dto.CreateAccountRequestDto;
import com.example.bankservice.entity.AppUser;
import com.example.bankservice.entity.BankAccount;
import com.example.bankservice.exception.AccountAlreadyExistsException;
import com.example.bankservice.exception.AccountNotFoundException;
import com.example.bankservice.messaging.AccountCreatedEvent;
import com.example.bankservice.messaging.AccountCreatedPublisher;
import com.example.bankservice.repository.AppUserRepository;
import com.example.bankservice.repository.BankRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.example.bankservice.enums.ActiveSituation;

@ExtendWith(MockitoExtension.class)
public class BankServiceTest {
    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private BankRepo bankRepo;

    @Mock
    private AccountCreatedPublisher accountCreatedPublisher;

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
    @ParameterizedTest
    @ValueSource(strings = {""," ", "  "})
    @DisplayName("Invalid usernames should reject")
    void createAccount_shouldRejectBlankUsername(String authenticatedUserName){
        requestDto.setUsername(authenticatedUserName);
        requestDto.setAccountNumber("99");
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
        assertEquals(0.0, result.getBalance());

        ArgumentCaptor<BankAccount> accountArgumentCaptor =
                ArgumentCaptor.forClass(BankAccount.class);

        verify(bankRepo).save(accountArgumentCaptor.capture());
        BankAccount savedAccount = accountArgumentCaptor.getValue();

        assertEquals("Ana Hesap", savedAccount.getName());
        assertEquals("TR123", savedAccount.getAccountNumber());
        assertEquals(0.0, savedAccount.getBalance());
        assertSame(appUser,savedAccount.getAppUser());  // bellekte aynı nesne mi

        // verify(bankRepo).save(any(BankAccount.class)); --- >> Parametre kontrolü yapmaz. Yanlış paramatere de kabıl eder


        //giden mesajı doğrulamak için
        ArgumentCaptor<AccountCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(AccountCreatedEvent.class);
        verify(accountCreatedPublisher).publish(eventCaptor.capture());

        AccountCreatedEvent publishedEvent = eventCaptor.getValue();
        assertEquals("Ana Hesap", publishedEvent.accountName());
        assertEquals("TR123", publishedEvent.accountNumber());
        assertEquals(authenticatedUserName, publishedEvent.username());
        assertNotNull(publishedEvent.createdAt());


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

}
