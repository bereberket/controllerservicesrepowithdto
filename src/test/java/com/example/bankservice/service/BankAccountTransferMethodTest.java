package com.example.bankservice.service;

import com.example.bankservice.dto.BankAccountResponseDto;
import com.example.bankservice.dto.TransferRequestDto;
import com.example.bankservice.dto.TransferResponseDto;
import com.example.bankservice.entity.AppUser;
import com.example.bankservice.entity.BankAccount;
import com.example.bankservice.entity.TransferRecord;
import com.example.bankservice.enums.ActiveSituation;
import com.example.bankservice.enums.Role;
import com.example.bankservice.exception.AccountNotFoundException;
import com.example.bankservice.repository.AppUserRepository;
import com.example.bankservice.repository.BankRepo;
import com.example.bankservice.repository.TransferRecordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BankAccountTransferMethodTest {
    @Mock
    private BankRepo bankRepo;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private OutboxService outboxService;

    @Mock
    private TransferRecordRepository transferRecordRepository;

    @InjectMocks
    private BankService bankService;

    private TransferRequestDto transferRequestDto;
    private AppUser appUser;
    private static final String authenticatedUserName = "sender user";

    @Test
    @DisplayName("If accounts exists and different then transfer should be success")
    void transfer_shouldTransferMoney_whenAccountsExistsAndDifferent(){
        //ARRANGE
        AppUser senderUser = new AppUser();
        senderUser.setUsername("sender user");
        senderUser.setId(39L);
        senderUser.setRole(Role.CUSTOMER);
        senderUser.setPassword("gönderici-hesabı");
        senderUser.setState(ActiveSituation.ACTIVE);


        AppUser recipientUser = new AppUser();
        recipientUser.setUsername("recipient user");
        recipientUser.setState(ActiveSituation.ACTIVE);
        recipientUser.setRole(Role.CUSTOMER);
        recipientUser.setPassword("alıcı-hesabı");
        recipientUser.setId(40L);

        BankAccount senderAccount = new BankAccount();
        senderAccount.setAppUser(senderUser);
        senderAccount.setAccountNumber("TR123");
        senderAccount.setName("sender-account");
        senderAccount.setBalance(new BigDecimal("200.00"));

        BankAccount recipientAccount = new BankAccount();
        recipientAccount.setAppUser(recipientUser);
        recipientAccount.setAccountNumber("TR456");
        recipientAccount.setName("recipient-account");
        recipientAccount.setBalance(new BigDecimal("200.00"));

        transferRequestDto = new TransferRequestDto();
        transferRequestDto.setAmount(new BigDecimal("100.00"));
        transferRequestDto.setSourceAccountNumber(senderAccount.getAccountNumber());
        transferRequestDto.setTargetAccountNumber(recipientAccount.getAccountNumber());

        when(bankRepo.findAccountForUpdate("TR123"))
                .thenReturn(Optional.of(senderAccount));

        when(bankRepo.findAccountForUpdate("TR456"))
                .thenReturn(Optional.of(recipientAccount));

        //ACT
        TransferResponseDto result =
                bankService.transfer(transferRequestDto, authenticatedUserName, null);
        assertEquals(new BigDecimal("100.00"), result.getSourceAfterTransfer());

        verify(bankRepo).findAccountForUpdate("TR123");
        ArgumentCaptor<TransferRecord> transferRecordCaptor =
                ArgumentCaptor.forClass(TransferRecord.class);
        verify(transferRecordRepository).save(transferRecordCaptor.capture());

        String generatedRequestId = transferRecordCaptor.getValue().getRequestId();
        assertEquals(generatedRequestId, UUID.fromString(generatedRequestId).toString());
        verify(outboxService).saveTransferMethodEvent(any());
    }

    @Test
    void transfer_shouldThrowException_whenAccountNotBelongToUser(){
        //ARRANGE
        AppUser mainUser = new AppUser();
        mainUser.setUsername("sender user");
        mainUser.setId(39L);
        mainUser.setRole(Role.CUSTOMER);
        mainUser.setPassword("gönderici-hesabı");
        mainUser.setState(ActiveSituation.ACTIVE);


        AppUser recipientUser = new AppUser();
        recipientUser.setUsername("recipient user");
        recipientUser.setState(ActiveSituation.ACTIVE);
        recipientUser.setRole(Role.CUSTOMER);
        recipientUser.setPassword("alıcı-hesabı");
        recipientUser.setId(40L);

        AppUser differUser = new AppUser();
        differUser.setUsername("different-user");
        differUser.setRole(Role.CUSTOMER);
        differUser.setPassword("yanlış-hesap");
        differUser.setState(ActiveSituation.ACTIVE);
        differUser.setId(41L);



        BankAccount senderAccount = new BankAccount();
        senderAccount.setAppUser(mainUser);
        senderAccount.setAccountNumber("TR123");
        senderAccount.setName("sender-account");
        senderAccount.setBalance(new BigDecimal("200.00"));

        BankAccount recipientAccount = new BankAccount();
        recipientAccount.setAppUser(recipientUser);
        recipientAccount.setAccountNumber("TR456");
        recipientAccount.setName("recipient-account");
        recipientAccount.setBalance(new BigDecimal("200.00"));

        transferRequestDto = new TransferRequestDto();
        transferRequestDto.setAmount(new BigDecimal("100.00"));
        transferRequestDto.setSourceAccountNumber(senderAccount.getAccountNumber());
        transferRequestDto.setTargetAccountNumber(recipientAccount.getAccountNumber());

        when(bankRepo.findAccountForUpdate("TR123"))
                .thenReturn(Optional.of(senderAccount));

        when(bankRepo.findAccountForUpdate("TR456"))
                .thenReturn(Optional.of(recipientAccount));

        AccountNotFoundException exception = assertThrows(
                AccountNotFoundException.class,
                ()-> bankService.transfer(
                        transferRequestDto,
                        "different-user",
                        "22222222-2222-2222-2222-222222222222"
                )
        );

        assertEquals("Account doesn't found!", exception.getMessage());

        assertEquals(new BigDecimal("200.00"), senderAccount.getBalance());
        assertEquals(new BigDecimal("200.00"), recipientAccount.getBalance());

        verify(bankRepo).findAccountForUpdate("TR123");
        verify(bankRepo).findAccountForUpdate("TR456");
        verify(bankRepo, never()).save(any(BankAccount.class));
        verify(transferRecordRepository, never()).save(any(TransferRecord.class));
        verifyNoInteractions(outboxService);
    }

    @Test
    void transfer_shouldReturnPreviousResult_whenRequestIdWasAlreadyProcessed() {
        TransferRequestDto request = new TransferRequestDto();
        request.setSourceAccountNumber("TR123");
        request.setTargetAccountNumber("TR456");
        request.setAmount(new BigDecimal("100.00"));

        String requestId = "33333333-3333-3333-3333-333333333333";
        TransferRecord existingTransfer = new TransferRecord(
                "44444444-4444-4444-4444-444444444444",
                requestId,
                authenticatedUserName,
                "TR123",
                "TR456",
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                new BigDecimal("300.00"),
                Instant.parse("2026-08-11T10:00:00Z")
        );

        when(transferRecordRepository.findByRequestId(requestId))
                .thenReturn(Optional.of(existingTransfer));

        TransferResponseDto result = bankService.transfer(
                request,
                authenticatedUserName,
                requestId
        );

        assertEquals(new BigDecimal("100.00"), result.getAmount());
        assertEquals(new BigDecimal("100.00"), result.getSourceAfterTransfer());
        assertEquals(existingTransfer.getCreatedAt(), result.getTransferredAt());

        verifyNoInteractions(bankRepo);
        verifyNoInteractions(outboxService);
        verify(transferRecordRepository, never()).save(any(TransferRecord.class));
    }




}
