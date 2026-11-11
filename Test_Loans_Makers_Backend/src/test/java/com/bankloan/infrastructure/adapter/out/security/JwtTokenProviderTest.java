package com.bankloan.infrastructure.adapter.out.security;

import com.bankloan.domain.port.out.TokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        String secret = "c2VjcmV0S2V5Rm9yRGV2ZWxvcG1lbnRPbmx5RG9Ob3RVc2VJblByb2R1Y3Rpb24xMjM0NTY3ODk=";
        tokenProvider = new JwtTokenProvider(secret, 3600000L);
    }

    @Test
    void shouldGenerateAndValidateToken() {
        UUID userId = UUID.randomUUID();
        String token = tokenProvider.generateToken(userId, "test@test.com", "ROLE_USER");

        assertThat(token).isNotBlank();

        TokenProvider.TokenClaims claims = tokenProvider.validateToken(token);

        assertThat(claims.userId()).isEqualTo(userId);
        assertThat(claims.email()).isEqualTo("test@test.com");
        assertThat(claims.role()).isEqualTo("ROLE_USER");
    }

    @Test
    void shouldRejectInvalidToken() {
        assertThatThrownBy(() -> tokenProvider.validateToken("invalid.token.here"))
                .isInstanceOf(JwtTokenProvider.InvalidTokenException.class);
    }

    @Test
    void shouldRejectExpiredToken() {
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(
                "c2VjcmV0S2V5Rm9yRGV2ZWxvcG1lbnRPbmx5RG9Ob3RVc2VJblByb2R1Y3Rpb24xMjM0NTY3ODk=",
                -1000L
        );

        UUID userId = UUID.randomUUID();
        String token = shortLivedProvider.generateToken(userId, "test@test.com", "ROLE_USER");

        assertThatThrownBy(() -> shortLivedProvider.validateToken(token))
                .isInstanceOf(JwtTokenProvider.InvalidTokenException.class);
    }
}
