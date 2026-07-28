package com.example.bankservice.service;

import com.example.bankservice.dto.BankAccountResponseDto;
import com.example.bankservice.dto.CreateAccountRequestDto;
import com.example.bankservice.entity.AppUser;
import com.example.bankservice.entity.BankAccount;
import com.example.bankservice.entity.OutboxEvent;
import com.example.bankservice.enums.ActiveSituation;
import com.example.bankservice.enums.OutboxStatus;
import com.example.bankservice.enums.Role;
import com.example.bankservice.exception.AccountAlreadyExistsException;
import com.example.bankservice.exception.AccountNotFoundException;
import com.example.bankservice.repository.AppUserRepository;
import com.example.bankservice.repository.BankRepo;
import com.example.bankservice.repository.OutboxRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AccountCreatingOutboxIntegrationTest {
    @Autowired
    private BankService bankService;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private BankRepo bankRepo;

    @Autowired
    private OutboxRepository outboxRepository;

    @Test
    void contextLoads(){
    }

    @Test
    void bankServiceBean_shouldBeLoaded(){
        assertNotNull(bankService);
    }

    @Test
    @Transactional
    void createAccount_shouldPersistAccountAndOutboxEvent_whenUserExists(){
        //arrange
        AppUser appUser = new AppUser();
        appUser.setUsername("integrasyon user");
        appUser.setPassword("berk10");
        appUser.setRole(Role.CUSTOMER);
        appUser.setState(ActiveSituation.ACTIVE);

        CreateAccountRequestDto requestDto =
                new CreateAccountRequestDto();

        requestDto.setName("integrasyon hesabı");
        requestDto.setAccountNumber("TR900");
        appUserRepository.save(appUser);

        //act
        BankAccountResponseDto result =
                bankService.createAccount(requestDto, "integrasyon user");

        //ASSERT
        assertEquals("integrasyon hesabı", result.getName());
        assertEquals("TR900", result.getAccountNumber());
        assertEquals(new BigDecimal("0.00"), result.getBalance());

        BankAccount savedAccount =
                bankRepo.findByAccountNumberAndAppUserUsername(
                        "TR900",
                        "integrasyon user"
                )
                        .orElseThrow();


        assertEquals("integrasyon hesabı",savedAccount.getName());
        assertEquals("TR900", savedAccount.getAccountNumber());
        assertEquals(new BigDecimal("0.00"), savedAccount.getBalance());
        assertEquals("integrasyon user",savedAccount.getAppUser().getUsername());
        assertEquals(appUser.getId(), savedAccount.getAppUser().getId());


        List<OutboxEvent>pendingEvents =
                outboxRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        OutboxEvent savedAccountOutboxEvent = pendingEvents.stream()
                .filter(event-> "TR900".equals(event.getAggregateId())).findFirst().orElseThrow();

        assertNotNull(savedAccountOutboxEvent.getEventId());

        assertEquals("TR900", savedAccountOutboxEvent.getAggregateId());
        assertEquals("ACCOUNT_CREATED", savedAccountOutboxEvent.getEventType());

        assertEquals(OutboxStatus.PENDING, savedAccountOutboxEvent.getStatus());

        assertEquals(0,savedAccountOutboxEvent.getRetryCount());

        assertNotNull(savedAccountOutboxEvent.getCreatedAt());
        assertNull(savedAccountOutboxEvent.getPublishedAt());
        assertNull(savedAccountOutboxEvent.getLastError());

        assertTrue(
                savedAccountOutboxEvent.getPayload()
                        .contains("\"accountNumber\":\"TR900\"")
        );

        assertTrue(
                savedAccountOutboxEvent.getPayload()
                        .contains("\"username\":\"integrasyon user\"")
        );
    }

    @Test
    @Transactional
    void crateAccount_ShouldThrowException_whenAccountNumberAlreadyExists(){
        //arrange
        AppUser appUser = new AppUser();
        appUser.setUsername("berk");
        appUser.setPassword("berk10");
        appUser.setRole(Role.CUSTOMER);
        appUser.setState(ActiveSituation.ACTIVE);

        CreateAccountRequestDto requestDto =
                new CreateAccountRequestDto();

        requestDto.setName("test account");
        requestDto.setAccountNumber("TR900");
        appUserRepository.save(appUser);

        //ACT
        BankAccountResponseDto firstResult =
                bankService.createAccount(requestDto, "berk");

        long outboxCountBefore = outboxRepository.findAll()
                .stream()
                .filter(event -> "TR900".equals(event.getAggregateId()))
                .count();

        AccountAlreadyExistsException exception = assertThrows(
                AccountAlreadyExistsException.class,
                () -> bankService.createAccount(requestDto, "berk")
        );

        //assert
        assertEquals("test account", firstResult.getName());
        assertEquals("TR900", firstResult.getAccountNumber());

        long accountCount = bankRepo.findAll()
                .stream()
                .filter(account -> "TR900".equals(account.getAccountNumber()))
                .count();

        long outboxCountAfter = outboxRepository.findAll()
                .stream()
                .filter(event -> "TR900".equals(event.getAggregateId()))
                .count();

        assertEquals(1, accountCount);
        assertEquals(1, outboxCountBefore);
        assertEquals(outboxCountBefore, outboxCountAfter);
    }

    @Test
    @Transactional
    void createAccount_shouldThrowException_whenUserNotFound(){
        CreateAccountRequestDto requestDto = new CreateAccountRequestDto();

        requestDto.setName("Ana Hesap");
        requestDto.setAccountNumber("TR149");

        long outboxCountBefore = outboxRepository.findAll()
                .stream()
                .filter(event -> "TR149".equals(event.getAggregateId()))
                .count();
        long accountCountBefore = bankRepo.findAll()
                .stream()
                .filter(account -> "TR149".equals(account.getAccountNumber()))
                .count();

        AccountNotFoundException exception = assertThrows(
                AccountNotFoundException.class,
                ()-> bankService.createAccount(requestDto, "olmayan-kullanıcı"));





        long accountCount = bankRepo.findAll()
                .stream()
                .filter(account -> "TR149".equals(account.getAccountNumber()))
                .count();

        long outboxCountAfter = outboxRepository.findAll()
                .stream()
                .filter(event -> "TR149".equals(event.getAggregateId()))
                .count();

        assertEquals(0, accountCount);
        assertEquals(0, outboxCountBefore);
        assertEquals(outboxCountBefore, outboxCountAfter);



    }




    }


