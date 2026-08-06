package com.example.bankservice.service;

import com.example.bankservice.Enums.TransferResult;
import com.example.bankservice.dto.TransferRequestDto;
import com.example.bankservice.entity.AppUser;
import com.example.bankservice.entity.BankAccount;
import com.example.bankservice.enums.ActiveSituation;
import com.example.bankservice.enums.Role;
import com.example.bankservice.exception.InsufficientBalanceException;
import com.example.bankservice.repository.AppUserRepository;
import com.example.bankservice.repository.BankRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class BankAccountTransferTest {
    @Autowired
    private BankService bankService;

    @Autowired
    private BankRepo bankRepo;


    @Autowired
    private AppUserRepository appUserRepository;

    @Test
    void concurrentTransfer_shouldAllowOnlyOneTransfer() throws Exception{
        String uniquePart = UUID.randomUUID()
                .toString()
                .substring(0,8);
        String beneficiaryUserName = "alıcı";
        String beneficiaryAccountNumber = "TR" +uniquePart.toUpperCase();

        String uniquePart1 = UUID.randomUUID()
                .toString()
                .substring(0,8);
        String senderUserName = "gönderici";
        String senderAccountNumber = "TR" +uniquePart1.toUpperCase();

        AppUser beneficiaryUser = new AppUser();
        beneficiaryUser.setUsername(beneficiaryUserName);
        beneficiaryUser.setPassword("test-password");
        beneficiaryUser.setRole(Role.CUSTOMER);
        beneficiaryUser.setState(ActiveSituation.ACTIVE);


        AppUser senderUser = new AppUser();
        senderUser.setUsername(senderUserName);
        senderUser.setPassword("test-password");
        senderUser.setRole(Role.CUSTOMER);
        senderUser.setState(ActiveSituation.ACTIVE);

        appUserRepository.saveAndFlush(beneficiaryUser);
        appUserRepository.saveAndFlush(senderUser);

        BankAccount beneficiaryAccount = new BankAccount();
        beneficiaryAccount.setName("Alıcı Hesap");
        beneficiaryAccount.setAccountNumber(beneficiaryAccountNumber);
        beneficiaryAccount.setBalance(new BigDecimal("100.00"));
        beneficiaryAccount.setAppUser(beneficiaryUser);

        BankAccount senderAccount = new BankAccount();
        senderAccount.setName("Gönderici Hesap");
        senderAccount.setAccountNumber(senderAccountNumber);
        senderAccount.setBalance(new BigDecimal("100.00"));
        senderAccount.setAppUser(senderUser);

        bankRepo.saveAndFlush(beneficiaryAccount);
        bankRepo.saveAndFlush(senderAccount);

        BigDecimal amount = new BigDecimal(80);

        String firstAccountNumber;
        String secondAccountNumber;

        if(senderAccountNumber.compareTo(beneficiaryAccountNumber)<0){
            firstAccountNumber = senderAccountNumber;
            secondAccountNumber = beneficiaryAccountNumber;
        }else{
            firstAccountNumber = beneficiaryAccountNumber;
            secondAccountNumber = senderAccountNumber;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch= new CountDownLatch(1);

        TransferRequestDto transferRequestDto = new TransferRequestDto();
        transferRequestDto.setAmount(amount);
        transferRequestDto.setSourceAccountNumber(senderAccountNumber);
        transferRequestDto.setTargetAccountNumber(beneficiaryAccountNumber);

        Callable<TransferResult> transferResultCallable  = () -> {
            readyLatch.countDown();
            startLatch.await();

            try{
                bankService.transfer(transferRequestDto, senderUserName);
                return TransferResult.SUCCESSFUL;
            }catch(InsufficientBalanceException exception){
                return TransferResult.INSUFFICIENT_BALANCE;
            }catch(ObjectOptimisticLockingFailureException exception){
                return TransferResult.OPTIMISTIC_CONFLICT;
            }

        };

        TransferResult firstResult;
        TransferResult secondResult;

        try{
            Future<TransferResult> firstFuture =
                    executorService.submit(transferResultCallable);

            Future<TransferResult> secondFuture =
                    executorService.submit(transferResultCallable);

            boolean bothThreadsReady = readyLatch.await(5,TimeUnit.SECONDS);

            assertTrue(bothThreadsReady,"Threads couldn't be ready");
            startLatch.countDown();

            firstResult = firstFuture.get(5,TimeUnit.SECONDS);
            secondResult = secondFuture.get(5,TimeUnit.SECONDS);
        }finally {
            executorService.shutdownNow();
        }

        List<TransferResult> resultList =
                List.of(firstResult,secondResult);

        assertEquals(1, Collections.frequency(resultList,TransferResult.INSUFFICIENT_BALANCE));
        assertEquals(1, Collections.frequency(resultList,TransferResult.SUCCESSFUL));
        assertEquals(0, Collections.frequency(resultList,TransferResult.OPTIMISTIC_CONFLICT));


        BankAccount updatedUser =
                bankRepo.findByAccountNumberAndAppUserUsername(
                        senderAccountNumber,
                        senderUserName
                ).orElseThrow();

        assertEquals(new BigDecimal("20.00"), updatedUser.getBalance());

        BankAccount alıcı =
                bankRepo.findByAccountNumberAndAppUserUsername(
                        beneficiaryAccountNumber,
                        beneficiaryUserName
                ).orElseThrow();
        assertEquals(new BigDecimal("180.00"),alıcı.getBalance());













    }
}
