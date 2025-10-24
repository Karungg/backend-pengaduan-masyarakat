package com.miftah.pengaduan_masyarakat.service;

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
import com.miftah.pengaduan_masyarakat.model.Role;
import com.miftah.pengaduan_masyarakat.model.User;
import com.miftah.pengaduan_masyarakat.repository.RoleRepository;
import com.miftah.pengaduan_masyarakat.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthenticationService {

    private final RoleRepository roleRepository;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    private final MessageSource messageSource;

    public AuthenticationService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            JwtService jwtService, MessageSource messageSource) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.messageSource = messageSource;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("Attempting to register new user with username: {}", request.getUsername());
        Locale currentLocale = LocaleContextHolder.getLocale();

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: Username {} already exists.", request.getUsername());

            String message = messageSource.getMessage("user.username.unique", null, "Username sudah terdaftar",
                    currentLocale);

            throw new ValidationException(message, Map.of("username", List.of(message)));
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email {} already exists.", request.getEmail());

            String message = messageSource.getMessage("user.email.unique", null, "Email sudah terdaftar",
                    currentLocale);

            throw new ValidationException(message, Map.of("email", List.of(message)));
        }

        log.debug("Looking for role: {}", request.getRole());
        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> {
                    log.error("Registration failed: Role '{}' not found.", request.getRole());

                    String message = messageSource.getMessage("user.role.notfound", null, "Nama role tidak ditemukan",
                            currentLocale);

                    return new ValidationException(message, Map.of("username", List.of(message)));
                });
        log.debug("Role found: {}", role.getName());

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

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
