package com.bankloan.infrastructure.config;

import com.bankloan.domain.model.Role;
import com.bankloan.domain.model.User;
import com.bankloan.domain.port.out.PasswordEncoder;
import com.bankloan.domain.port.out.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private DataSeeder dataSeeder;

    @Test
    void run_shouldSeedUsersWhenNotExist() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        dataSeeder.run();

        verify(userRepository, times(2)).save(any(User.class));
        verify(passwordEncoder, times(2)).encode("123");
    }

    @Test
    void run_shouldSkipExistingUsers() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        dataSeeder.run();

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void run_shouldSeedOnlyMissingUsers() {
        when(userRepository.existsByEmail("usuario@test.com")).thenReturn(true);
        when(userRepository.existsByEmail("admin@test.com")).thenReturn(false);
        when(passwordEncoder.encode("123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        dataSeeder.run();

        verify(userRepository, times(1)).save(any(User.class));
    }
}
