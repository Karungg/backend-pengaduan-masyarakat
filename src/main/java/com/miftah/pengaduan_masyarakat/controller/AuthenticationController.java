package com.miftah.pengaduan_masyarakat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.miftah.pengaduan_masyarakat.dto.LoginRequest;
import com.miftah.pengaduan_masyarakat.dto.LoginResponse;
import com.miftah.pengaduan_masyarakat.dto.RegisterRequest;
import com.miftah.pengaduan_masyarakat.dto.UserResponse;
import com.miftah.pengaduan_masyarakat.model.User;
import com.miftah.pengaduan_masyarakat.service.AuthenticationService;
import com.miftah.pengaduan_masyarakat.service.JwtService;

@RequestMapping("api/v1/auth")
@RestController
public class AuthenticationController {
    private final JwtService jwtService;

    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        UserResponse registeredUser = authenticationService.register(request);

        return ResponseEntity.ok().body(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginRequest request) {
        User authenticatedUser = authenticationService.authenticate(request);

        String jwtToken = jwtService.generateToken(authenticatedUser);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(jwtToken);
        loginResponse.setExpiresIn(jwtService.getExpirationTime());

        return ResponseEntity.ok(loginResponse);
    }
}
