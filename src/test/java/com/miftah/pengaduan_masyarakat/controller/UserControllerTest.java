package com.miftah.pengaduan_masyarakat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miftah.pengaduan_masyarakat.dto.UserRequest;
import com.miftah.pengaduan_masyarakat.model.User;
import com.miftah.pengaduan_masyarakat.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setUsername("budisantoso");
        existingUser.setEmail("budi.santoso@example.com");
        existingUser.setPassword(passwordEncoder.encode("password123"));
        userRepository.save(existingUser);
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/users - Should create user successfully")
    void createUser_whenValidRequest_shouldReturn201AndUserData() throws Exception {
        UserRequest newUserRequest = new UserRequest("sitilestari", "siti.lestari@example.com", "passwordkuat456");

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/users/")))
                .andExpect(jsonPath("$.data.username").value("sitilestari"))
                .andExpect(jsonPath("$.data.email").value("siti.lestari@example.com"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/users - Should fail if username already exists")
    void createUser_whenUsernameExists_shouldReturn400() throws Exception {
        UserRequest duplicateUserRequest = new UserRequest("budisantoso", "password123", "budi.baru@example.com");

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateUserRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/users - Should return list of users")
    void getAllUsers_shouldReturnUserList() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].username").value("budisantoso"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/users/{id} - Should return user when ID exists")
    void getUserById_whenIdExists_shouldReturnUser() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", existingUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(existingUser.getId().toString()))
                .andExpect(jsonPath("$.data.username").value("budisantoso"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/users/{id} - Should return 404 when ID does not exist")
    void getUserById_whenIdDoesNotExist_shouldReturn404() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/users/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/v1/users/{id} - Should update user successfully")
    void updateUser_whenValidRequest_shouldReturnUpdatedUser() throws Exception {
        UserRequest updateRequest = new UserRequest("budisantoso_updated", "budi.updated@example.com",
                "passwordkuat456");

        mockMvc.perform(put("/api/v1/users/{id}", existingUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("budisantoso_updated"))
                .andExpect(jsonPath("$.data.email").value("budi.updated@example.com"));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/v1/users/{id} - Should delete user successfully")
    void deleteUser_whenIdExists_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{id}", existingUser.getId()))
                .andExpect(status().isNoContent());

        assertFalse(userRepository.existsById(existingUser.getId()));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/v1/users/{id} - Should return 404 when ID does not exist")
    void deleteUser_whenIdDoesNotExist_shouldReturn404() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/users/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }
}
