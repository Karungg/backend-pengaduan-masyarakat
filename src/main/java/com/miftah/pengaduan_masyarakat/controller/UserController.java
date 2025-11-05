package com.miftah.pengaduan_masyarakat.controller;

import com.miftah.pengaduan_masyarakat.dto.GenericResponse;
import com.miftah.pengaduan_masyarakat.dto.UserRequest;
import com.miftah.pengaduan_masyarakat.dto.UserResponse;
import com.miftah.pengaduan_masyarakat.enums.RoleEnum;
import com.miftah.pengaduan_masyarakat.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Slf4j
@RequiredArgsConstructor

public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<GenericResponse<UserResponse>> createUser(@Valid @RequestBody UserRequest request) {
        log.info("Received request to create user with username: {}", request.getUsername());
        UserResponse createdUser = userService.createUser(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdUser.getId())
                .toUri();

        return ResponseEntity.created(location)
                .body(GenericResponse.created(createdUser));
    }

    @GetMapping
    public ResponseEntity<GenericResponse<List<UserResponse>>> getAllUsers(
            @RequestParam(required = false) RoleEnum role) {
        log.info("Received request to fetch all users with role : {}", role);

        List<UserResponse> users = userService.getAllUsers(role);

        return ResponseEntity.ok(GenericResponse.ok(users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        log.info("Received request to fetch user with ID: {}", id);

        UserResponse user = userService.getUserById(id);

        return ResponseEntity.ok(GenericResponse.ok(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenericResponse<UserResponse>> updateUser(@PathVariable UUID id,
            @Valid @RequestBody UserRequest request) {
        log.info("Received request to update user with ID: {}", id);

        UserResponse updatedUser = userService.updateUser(id, request);

        return ResponseEntity.ok(GenericResponse.ok(updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponse<Void>> deleteUser(@PathVariable UUID id) {
        log.info("Received request to delete user with ID: {}", id);

        userService.deleteUser(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(GenericResponse.noContent());
    }
}