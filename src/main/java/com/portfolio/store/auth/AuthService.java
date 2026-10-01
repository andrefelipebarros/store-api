package com.portfolio.store.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.portfolio.store.auth.AuthDtos.LoginRequest;
import com.portfolio.store.auth.AuthDtos.RegisterRequest;
import com.portfolio.store.auth.AuthDtos.TokenResponse;
import com.portfolio.store.exception.BusinessException;
import com.portfolio.store.exception.NotFoundException;
import com.portfolio.store.security.JwtService;
import com.portfolio.store.user.Role;
import com.portfolio.store.user.User;
import com.portfolio.store.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email já cadastrado");
        }
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.CUSTOMER);
        userRepository.save(user);
        return new TokenResponse(jwtService.generateToken(user.getEmail(), user.getRole().name()));
    }

    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        return new TokenResponse(jwtService.generateToken(user.getEmail(), user.getRole().name()));
    }
}
