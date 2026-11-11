package com.bankloan.application.command;

import com.bankloan.domain.exception.InvalidCredentialsException;
import com.bankloan.domain.exception.UserDisabledException;
import com.bankloan.domain.exception.UserNotFoundException;
import com.bankloan.domain.model.Role;
import com.bankloan.domain.model.User;
import com.bankloan.domain.port.in.AuthUseCase;
import com.bankloan.domain.port.out.PasswordEncoder;
import com.bankloan.domain.port.out.TokenProvider;
import com.bankloan.domain.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthCommandServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenProvider tokenProvider;

    private AuthCommandService authCommandService;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        authCommandService = new AuthCommandService(userRepository, passwordEncoder, tokenProvider);
        userId = UUID.randomUUID();
        testUser = User.reconstitute(
                userId, "test@test.com", "$2a$10$encodedHash",
                Role.ROLE_USER, true, Instant.now(), Instant.now()
        );
    }

    @Test
    void loginShouldReturnTokenForValidCredentials() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("123", "$2a$10$encodedHash")).thenReturn(true);
        when(tokenProvider.generateToken(eq(userId), eq("test@test.com"), eq("ROLE_USER")))
                .thenReturn("jwt-token");

        AuthUseCase.AuthResult result = authCommandService.login("test@test.com", "123");

        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.user().getEmail()).isEqualTo("test@test.com");
    }

    @Test
    void loginShouldThrowForInvalidEmail() {
        when(userRepository.findByEmail("wrong@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authCommandService.login("wrong@test.com", "123"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void loginShouldThrowForInvalidPassword() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong", "$2a$10$encodedHash")).thenReturn(false);

        assertThatThrownBy(() -> authCommandService.login("test@test.com", "wrong"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void loginShouldThrowForDisabledUser() {
        User disabledUser = User.reconstitute(
                userId, "disabled@test.com", "$2a$10$encodedHash",
                Role.ROLE_USER, false, Instant.now(), Instant.now()
        );
        when(userRepository.findByEmail("disabled@test.com")).thenReturn(Optional.of(disabledUser));

        assertThatThrownBy(() -> authCommandService.login("disabled@test.com", "123"))
                .isInstanceOf(UserDisabledException.class);
    }

    @Test
    void getCurrentUserShouldReturnUserForValidId() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        User result = authCommandService.getCurrentUser(userId.toString());

        assertThat(result.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    void getCurrentUserShouldThrowForInvalidId() {
        UUID unknownId = UUID.randomUUID();
        when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authCommandService.getCurrentUser(unknownId.toString()))
                .isInstanceOf(UserNotFoundException.class);
    }
}
