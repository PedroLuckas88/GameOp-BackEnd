package com.gameop.service;

import com.gameop.dto.AuthResponse;
import com.gameop.dto.LoginRequest;
import com.gameop.dto.RegisterRequest;
import com.gameop.dto.UserResponse;
import com.gameop.entity.User;
import com.gameop.exception.ApiException;
import com.gameop.repository.UserRepository;
import com.gameop.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ApiException(HttpStatus.CONFLICT, "Este username já está em uso.");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Este e-mail já está cadastrado.");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();
        userRepository.save(user);
        return toAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = findForLogin(request);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos.");
        }
        return toAuthResponse(user);
    }

    private User findForLogin(LoginRequest request) {
        String email = request.email() == null ? "" : request.email().trim();
        String username = request.username() == null ? "" : request.username().trim();

        if (!email.isEmpty()) {
            return userRepository
                    .findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos."));
        }
        if (!username.isEmpty()) {
            if (username.contains("@")) {
                return userRepository
                        .findByEmailIgnoreCase(username)
                        .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos."));
            }
            return userRepository
                    .findByUsernameIgnoreCase(username)
                    .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos."));
        }
        throw new ApiException(HttpStatus.BAD_REQUEST, "Informe o usuário ou o e-mail.");
    }

    public UserResponse toUserResponse(User user) {
        return UserResponse.from(user);
    }

    private AuthResponse toAuthResponse(User user) {
        return new AuthResponse(
                jwtService.generateToken(user), "Bearer", jwtService.getExpirationSeconds(), UserResponse.from(user));
    }
}
