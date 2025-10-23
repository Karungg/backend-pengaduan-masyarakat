package com.miftah.pengaduan_masyarakat.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.miftah.pengaduan_masyarakat.dto.LoginRequest;
import com.miftah.pengaduan_masyarakat.dto.RegisterRequest;
import com.miftah.pengaduan_masyarakat.dto.UserResponse;
import com.miftah.pengaduan_masyarakat.model.Role;
import com.miftah.pengaduan_masyarakat.model.User;
import com.miftah.pengaduan_masyarakat.repository.RoleRepository;
import com.miftah.pengaduan_masyarakat.repository.UserRepository;

@Service
public class AuthenticationService {

    private final RoleRepository roleRepository;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse register(RegisterRequest request) {
        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        return convertToUserResponse(user);
    }

    public User authenticate(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        return userRepository.findByUsername(request.getUsername())
                .orElseThrow();
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
}
