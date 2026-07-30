package com.example.bankservice.config;

import com.example.bankservice.entity.AppUser;
import com.example.bankservice.enums.ActiveSituation;
import com.example.bankservice.enums.Role;
import com.example.bankservice.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private AppUserRepository appUserRepository;

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void userDetailsService_shouldEnableActiveUser() {
        AppUser appUser = createUser(ActiveSituation.ACTIVE);

        when(appUserRepository.findByUsername("berk"))
                .thenReturn(Optional.of(appUser));

        UserDetails userDetails = loadUser("berk");

        assertTrue(userDetails.isEnabled());
    }

    @Test
    void userDetailsService_shouldDisableInactiveUser() {
        AppUser appUser = createUser(ActiveSituation.INACTIVE);

        when(appUserRepository.findByUsername("berk"))
                .thenReturn(Optional.of(appUser));

        UserDetails userDetails = loadUser("berk");

        assertFalse(userDetails.isEnabled());
    }

    private UserDetails loadUser(String username) {
        UserDetailsService userDetailsService =
                securityConfig.userDetailsService(appUserRepository);

        return userDetailsService.loadUserByUsername(username);
    }

    private AppUser createUser(ActiveSituation state) {
        AppUser appUser = new AppUser();
        appUser.setUsername("berk");
        appUser.setPassword("encoded-password");
        appUser.setRole(Role.CUSTOMER);
        appUser.setState(state);
        return appUser;
    }
}
