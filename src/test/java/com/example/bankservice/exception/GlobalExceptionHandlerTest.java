package com.example.bankservice.exception;

import com.example.bankservice.controller.BankController;
import com.example.bankservice.entity.BankAccount;
import com.example.bankservice.service.AuthService;
import com.example.bankservice.service.BankService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {
    @Mock
    private BankService bankService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private BankController bankController;


    private MockMvc mockMvc;

    @BeforeEach
    void setUp(){
        mockMvc = MockMvcBuilders
                .standaloneSetup(bankController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void withdraw_shouldReturn409_OptimisticLockingFailure() throws Exception {
        //arrange
        ObjectOptimisticLockingFailureException exception = new ObjectOptimisticLockingFailureException(
                BankAccount.class,
                1L);
        when(
                bankService.withdraw(
                        "TR100",
                        new BigDecimal("80.00"),
                        "berk"
                )
        ).thenThrow(exception);

        //act
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "berk",
                "test-password"
        );

        mockMvc.perform(
                        post("/api/accounts/{accountNumber}/withdraw", "TR100")
                                .param("amount", "80.00")
                                .principal(authentication)
                )
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(
                        "The account was changed by another operation. Please try again."
                ));

        verify(bankService).withdraw(
                "TR100",
                new BigDecimal("80.00"),
                "berk"
        );
        verifyNoInteractions(authService);


    }


}
