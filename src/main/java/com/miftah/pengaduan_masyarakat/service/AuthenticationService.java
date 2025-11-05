package com.miftah.pengaduan_masyarakat.service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.miftah.pengaduan_masyarakat.dto.LoginRequest;
import com.miftah.pengaduan_masyarakat.dto.LoginResponse;
import com.miftah.pengaduan_masyarakat.dto.RegisterRequest;
import com.miftah.pengaduan_masyarakat.dto.UserResponse;
import com.miftah.pengaduan_masyarakat.exception.ValidationException;
import com.miftah.pengaduan_masyarakat.model.User;
import com.miftah.pengaduan_masyarakat.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MessageSource messageSource;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("Attempting to register new user with username: {}", request.getUsername());
        Locale currentLocale = LocaleContextHolder.getLocale();

        Map<String, List<String>> errors = new HashMap<>();

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: Username {} already exists.", request.getUsername());

            String message = messageSource.getMessage("user.username.unique", null, "Username sudah terdaftar",
                    currentLocale);

            errors.put("username", List.of(message));
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email {} already exists.", request.getEmail());

            String message = messageSource.getMessage("user.email.unique", null, "Email sudah terdaftar",
                    currentLocale);

            errors.put("email", List.of(message));
        }

        if (!errors.isEmpty()) {
            log.warn("Register failed due to validation errors: {}", errors);
            throw new ValidationException("Validation failed", errors);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        return convertToUserResponse(savedUser);
    }

    public LoginResponse login(LoginRequest request) {
        log.info("Attempting to authenticate user: {}", request.getUsername());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));
        log.debug("Authentication successful for user: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.error("User {} authenticated but not found in repository.", request.getUsername());
                    return new IllegalStateException("User not found after successful authentication.");
                });

        log.debug("Generating JWT token for user: {}", user.getUsername());
        String jwtToken = jwtService.generateToken(user);

        log.info("Login successful for user: {}", user.getUsername());

        return convertToLoginResponse(jwtToken, jwtService.getExpirationTime());
    }

    public UserResponse convertToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    public LoginResponse convertToLoginResponse(String token, long expirationTime) {
        return new LoginResponse(
                token, expirationTime);
    }
}
