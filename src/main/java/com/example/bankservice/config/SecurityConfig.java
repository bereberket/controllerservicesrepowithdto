package com.example.bankservice.config;

import com.example.bankservice.enums.Role;
import com.example.bankservice.repository.AppUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.bankservice.entity.AppUser;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.authorization.SingleResultAuthorizationManager.permitAll;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            AppUserRepository appUserRepository

    ) {
        return username -> {

            AppUser appUser = appUserRepository
                    .findByUsername(username)
                    .orElseThrow(() ->
                            new UsernameNotFoundException(
                                    "User not found :" + username
                            )
                    );
            return User.builder()
                    .username(appUser.getUsername())
                    .password(appUser.getPassword())
                    .roles(appUser.getRole().name())
                    .build();

        };
    }
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception{
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/h2-console/**")
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/accounts/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/accounts/all"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                "/api/accounts/**"

                        ).hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/admin/outbox/**"
                        ).hasRole("ADMIN")

                        .anyRequest().authenticated()
                )


                .headers(headers ->headers
                                .frameOptions(frameOptionsConfig -> frameOptionsConfig.sameOrigin())
                )

                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

}














