package com.portfolio.store.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.portfolio.store.auth.AuthDtos.LoginRequest;
import com.portfolio.store.auth.AuthDtos.RegisterRequest;
import com.portfolio.store.auth.AuthDtos.TokenResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
