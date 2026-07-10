package com.example.bankservice.service;

import com.example.bankservice.dto.BankAccountResponseDto;
import com.example.bankservice.dto.CreateAccountRequestDto;
import com.example.bankservice.entity.AppUser;
import com.example.bankservice.entity.BankAccount;
import com.example.bankservice.enums.Role;
import com.example.bankservice.exception.AccountAlreadyExistsException;
import com.example.bankservice.repository.AppUserRepository;
import com.example.bankservice.repository.BankRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class BankServiceAppTest {

    @Autowired
    private BankService bankService;
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private BankRepo bankRepo;


    @Test
    void contextLoads(){
    }

    @Test
    void bankServiceBean_shouldBeLoaded(){
        assertNotNull(bankService);
    }

    @Test
    @Transactional
    void createAccount_shouldPersistAccount_whenUserExists(){

        //ARRANGE
        AppUser appUser = new AppUser();
        appUser.setUsername("integrasyon user");
        appUser.setPassword("berk10");
        appUser.setRole(Role.CUSTOMER);

        appUserRepository.save(appUser);

        CreateAccountRequestDto requestDto =
                new CreateAccountRequestDto();

        requestDto.setName("integrasyon hesabı");
        requestDto.setAccountNumber("900");

        //ACT
        BankAccountResponseDto result =
                bankService.createAccount(requestDto, "integrasyon user");

        //ASSERT
        assertEquals("integrasyon hesabı", result.getName());
        assertEquals("TR900", result.getAccountNumber());
        assertEquals(0.0, result.getBalance());

        BankAccount savedAccount =
                bankRepo.findByAccountNumber("TR900")
                        .orElseThrow();


        assertEquals("integrasyon hesabı",savedAccount.getName());
        assertEquals("TR900", savedAccount.getAccountNumber());
        assertEquals(0.0, savedAccount.getBalance());
        assertEquals("integrasyon user",savedAccount.getAppUser().getUsername());
        assertEquals(appUser.getId(), savedAccount.getAppUser().getId());

    }
    @Test
    void createAccount_shouldThrowException_whenAccountNumberAlreadyExists(){
        AppUser appUser = new AppUser();
        appUser.setUsername("berko");
        appUser.setRole(Role.CUSTOMER);
        appUser.setPassword("berk10");

        appUserRepository.save(appUser);

        CreateAccountRequestDto firstRequestDto = new CreateAccountRequestDto();
        firstRequestDto.setAccountNumber("2573");
        firstRequestDto.setName("Birinci Hesap");
        firstRequestDto.setUsername("berko");

        bankService.createAccount(firstRequestDto,"berko");

        CreateAccountRequestDto secondReq = new CreateAccountRequestDto();
        secondReq.setUsername("berko");
        secondReq.setAccountNumber("2573");
        secondReq.setName("İkinci Hesap");


        AccountAlreadyExistsException exception = assertThrows(
                AccountAlreadyExistsException.class,
                () -> bankService.createAccount(secondReq,"berko")
                );

        assertEquals("This account number exists", exception.getMessage());

        BankAccount savedAccount = bankRepo.findByAccountNumber("TR2573")
                .orElseThrow();

        assertEquals(
                "Birinci Hesap",
                savedAccount.getName()
        );

    }
}
