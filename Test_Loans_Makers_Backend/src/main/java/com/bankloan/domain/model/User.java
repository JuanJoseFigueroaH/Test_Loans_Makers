package com.bankloan.domain.model;

import java.time.Instant;
import java.util.UUID;

public class User {

    private UUID id;
    private String email;
    private String passwordHash;
    private Role role;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

    private User() {}

    public static User create(String email, String passwordHash, Role role) {
        User user = new User();
        user.id = UUID.randomUUID();
        user.email = email;
        user.passwordHash = passwordHash;
        user.role = role;
        user.enabled = true;
        user.createdAt = Instant.now();
        user.updatedAt = Instant.now();
        return user;
    }

    public static User reconstitute(UUID id, String email, String passwordHash, Role role,
                                     boolean enabled, Instant createdAt, Instant updatedAt) {
        User user = new User();
        user.id = id;
        user.email = email;
        user.passwordHash = passwordHash;
        user.role = role;
        user.enabled = enabled;
        user.createdAt = createdAt;
        user.updatedAt = updatedAt;
        return user;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
