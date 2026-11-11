package com.bankloan.application.command;

import com.bankloan.domain.exception.InvalidCredentialsException;
import com.bankloan.domain.exception.UserDisabledException;
import com.bankloan.domain.model.User;
import com.bankloan.domain.port.in.AuthUseCase;
import com.bankloan.domain.port.out.PasswordEncoder;
import com.bankloan.domain.port.out.TokenProvider;
import com.bankloan.domain.port.out.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthCommandService implements AuthUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public AuthCommandService(UserRepository userRepository,
                              PasswordEncoder passwordEncoder,
                              TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResult login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.isEnabled()) {
            throw new UserDisabledException();
        }

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = tokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        return new AuthResult(token, user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser(String userId) {
        return userRepository.findById(java.util.UUID.fromString(userId))
                .orElseThrow(() -> new com.bankloan.domain.exception.UserNotFoundException(userId));
    }
}
