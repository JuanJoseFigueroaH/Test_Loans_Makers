package com.bankloan.domain.port.out;

import com.bankloan.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<User> findByEmail(String email);

    Optional<User> findById(UUID id);

    User save(User user);

    boolean existsByEmail(String email);
}
