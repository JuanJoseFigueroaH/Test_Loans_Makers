package com.bankloan.infrastructure.adapter.in.rest;

import com.bankloan.application.dto.LoginResponse;
import com.bankloan.application.dto.UserResponse;
import com.bankloan.application.mapper.UserMapper;
import com.bankloan.domain.model.Role;
import com.bankloan.domain.model.User;
import com.bankloan.domain.port.in.AuthUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthUseCase authUseCase;
    @Mock private UserMapper userMapper;
    @Mock private Authentication authentication;

    @InjectMocks private AuthController controller;

    private User testUser;
    private UserResponse testUserResponse;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        testUser = User.reconstitute(userId, "test@test.com", "hash", Role.ROLE_USER, true, Instant.now(), Instant.now());
        testUserResponse = new UserResponse(userId, "test@test.com", "ROLE_USER", true, Instant.now());
    }

    @Test
    void login_shouldReturnLoginResponse() {
        var request = new com.bankloan.application.dto.LoginRequest("test@test.com", "password");
        var authResult = new AuthUseCase.AuthResult("jwt-token", testUser);

        when(authUseCase.login("test@test.com", "password")).thenReturn(authResult);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        StepVerifier.create(controller.login(request))
                .assertNext(response -> {
                    assertThat(response.token()).isEqualTo("jwt-token");
                    assertThat(response.user().email()).isEqualTo("test@test.com");
                })
                .verifyComplete();

        verify(authUseCase).login("test@test.com", "password");
    }

    @Test
    void me_shouldReturnCurrentUser() {
        when(authentication.getName()).thenReturn(userId.toString());
        when(authUseCase.getCurrentUser(userId.toString())).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        StepVerifier.create(controller.me(authentication))
                .assertNext(response -> {
                    assertThat(response.email()).isEqualTo("test@test.com");
                    assertThat(response.role()).isEqualTo("ROLE_USER");
                })
                .verifyComplete();
    }
}
