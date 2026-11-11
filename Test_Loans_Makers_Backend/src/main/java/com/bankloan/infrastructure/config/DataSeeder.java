package com.bankloan.infrastructure.config;

import com.bankloan.domain.model.Role;
import com.bankloan.domain.model.User;
import com.bankloan.domain.port.out.PasswordEncoder;
import com.bankloan.domain.port.out.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.seeder.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedUser("usuario@test.com", "123", Role.ROLE_USER);
        seedUser("admin@test.com", "123", Role.ROLE_ADMIN);
        log.info("Development seed data loaded successfully");
    }

    private void seedUser(String email, String rawPassword, Role role) {
        if (userRepository.existsByEmail(email)) {
            log.info("User {} already exists, skipping", email);
            return;
        }

        String hash = passwordEncoder.encode(rawPassword);
        User user = User.create(email, hash, role);
        userRepository.save(user);
        log.info("Seeded user: {} with role {}", email, role);
    }
}
