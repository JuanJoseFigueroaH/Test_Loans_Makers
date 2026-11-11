package com.bankloan.infrastructure.adapter.in.rest;

import com.bankloan.application.dto.LoginRequest;
import com.bankloan.application.dto.LoginResponse;
import com.bankloan.application.dto.UserResponse;
import com.bankloan.application.mapper.UserMapper;
import com.bankloan.domain.model.User;
import com.bankloan.domain.port.in.AuthUseCase;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthUseCase authUseCase;
    private final UserMapper userMapper;

    public AuthController(AuthUseCase authUseCase, UserMapper userMapper) {
        this.authUseCase = authUseCase;
        this.userMapper = userMapper;
    }

    @PostMapping("/login")
    public Mono<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Mono.fromCallable(() -> {
            AuthUseCase.AuthResult result = authUseCase.login(request.email(), request.password());
            UserResponse userResponse = userMapper.toResponse(result.user());
            return new LoginResponse(result.token(), userResponse);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/me")
    public Mono<UserResponse> me(Authentication authentication) {
        return Mono.fromCallable(() -> {
            String userId = authentication.getName();
            User user = authUseCase.getCurrentUser(userId);
            return userMapper.toResponse(user);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
