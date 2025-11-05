package com.miftah.pengaduan_masyarakat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.miftah.pengaduan_masyarakat.dto.GenericResponse;
import com.miftah.pengaduan_masyarakat.dto.LoginRequest;
import com.miftah.pengaduan_masyarakat.dto.LoginResponse;
import com.miftah.pengaduan_masyarakat.dto.RegisterRequest;
import com.miftah.pengaduan_masyarakat.dto.UserResponse;
import com.miftah.pengaduan_masyarakat.service.AuthenticationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/api/v1/auth")
@RestController
@Slf4j
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<GenericResponse<UserResponse>> register(@RequestBody @Valid RegisterRequest request) {
        UserResponse registeredUser = authenticationService.register(request);

        return ResponseEntity.ok().body(GenericResponse.ok(registeredUser));
    }

    @PostMapping("/login")
    public ResponseEntity<GenericResponse<LoginResponse>> authenticate(@RequestBody @Valid LoginRequest request) {
        log.info("Attempting to login with username " + request.getUsername());

        LoginResponse authenticatedUser = authenticationService.login(request);

        return ResponseEntity.ok().body(GenericResponse.ok(authenticatedUser));
    }
}
