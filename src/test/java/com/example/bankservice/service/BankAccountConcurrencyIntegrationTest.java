package com.example.bankservice.service;

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
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")


public class BankAccountConcurrencyIntegrationTest {
    @Autowired
    private BankService bankService;

    @Autowired
    private BankRepo bankRepo;


    @Autowired
    private AppUserRepository appUserRepository;


    @Test
    void concurrentWithdraw_shouldAllowOnlyOneWithdrawal() throws Exception {
    String uniquePart = UUID.randomUUID()
            .toString()
            .substring(0,8);
    String username = "race-user" + uniquePart;
    String accountNumber = "TRRACE" +uniquePart.toUpperCase();

        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setPassword("test-password");
        appUser.setRole(Role.CUSTOMER);
        appUser.setState(ActiveSituation.ACTIVE);

        appUserRepository.saveAndFlush(appUser);

        BankAccount bankAccount = new BankAccount();
        bankAccount.setName("Race Condition Account");
        bankAccount.setAccountNumber(accountNumber);
        bankAccount.setBalance(new BigDecimal("100.00"));
        bankAccount.setAppUser(appUser);

        bankRepo.saveAndFlush(bankAccount);
        Long initialVersion = bankAccount.getVersion();

        assertNotNull(initialVersion);

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        Callable<Boolean> withdrawTask = ()-> {
            readyLatch.countDown();
            startLatch.await();


            try {
                bankService.withdraw(accountNumber, new BigDecimal("80.0"), username);
                return true;
            } catch (InsufficientBalanceException | ObjectOptimisticLockingFailureException exception){
                return false;
            }

        };

        boolean firstSuccessful;
        boolean secondSuccessful;

        try{
            Future<Boolean> firstFuture =
                    executorService.submit(withdrawTask);

            Future<Boolean> secondFuture =
                    executorService.submit(withdrawTask);

            boolean bothThreadsAreReady =
                    readyLatch.await(5, TimeUnit.SECONDS);

            assertTrue(
                    bothThreadsAreReady,
                    "Worker threads could not become ready in time"
            );
            startLatch.countDown();

            firstSuccessful =
                    firstFuture.get(5, TimeUnit.SECONDS);

            secondSuccessful =
                    secondFuture.get(5, TimeUnit.SECONDS);
        } finally {
            executorService.shutdownNow();
        }

        int successfulWithdrawalCount = 0;

        if(firstSuccessful){
            successfulWithdrawalCount++;
        }
        if(secondSuccessful){
            successfulWithdrawalCount++;
        }

        assertEquals(1,successfulWithdrawalCount,"Only one withdrawal should sucedded");

        BankAccount updatedAccount =
                bankRepo.findByAccountNumberAndAppUserUsername(
                        accountNumber,
                        username
                ).orElseThrow();

        assertEquals(
                new BigDecimal("20.00"),
                updatedAccount.getBalance()
        );





    }


}
