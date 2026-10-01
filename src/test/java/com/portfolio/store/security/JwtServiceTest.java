package com.portfolio.store.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "uma-chave-de-teste-com-mais-de-32-caracteres!!";

    @Test
    void shouldGenerateAndValidateToken() {
        JwtService service = new JwtService(SECRET, 60_000);

        String token = service.generateToken("ana@email.com", "CUSTOMER");

        assertThat(service.isValid(token)).isTrue();
        assertThat(service.extractEmail(token)).isEqualTo("ana@email.com");
    }

    @Test
    void shouldRejectTamperedToken() {
        JwtService service = new JwtService(SECRET, 60_000);

        String token = service.generateToken("ana@email.com", "CUSTOMER");

        assertThat(service.isValid(token + "x")).isFalse();
    }

    @Test
    void shouldRejectExpiredToken() {
        JwtService service = new JwtService(SECRET, -1_000);

        String token = service.generateToken("ana@email.com", "CUSTOMER");

        assertThat(service.isValid(token)).isFalse();
    }
}
