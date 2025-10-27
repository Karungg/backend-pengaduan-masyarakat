package com.miftah.pengaduan_masyarakat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miftah.pengaduan_masyarakat.dto.LoginRequest;
import com.miftah.pengaduan_masyarakat.dto.RegisterRequest;
import com.miftah.pengaduan_masyarakat.enums.RoleEnum;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthenticationControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        private RoleEnum userRole = RoleEnum.USER;
        private final String existingUsername = "existinguser";
        private final String existingEmail = "existing@example.com";
        private final String rawPassword = "password123";

        @BeforeEach
        void setUp() {

                User user = new User();
                user.setUsername(existingUsername);
                user.setEmail(existingEmail);
                user.setPassword(passwordEncoder.encode(rawPassword));
                user.setRole(userRole);
                userRepository.save(user);
        }

        @Test
        @DisplayName("POST /api/v1/auth/register - Success")
        void register_whenValidRequest_shouldReturnOkAndUserData() throws Exception {
                RegisterRequest request = RegisterRequest.builder()
                                .username("newuser")
                                .email("newuser@example.com")
                                .password("passwordkuat456")
                                .role(userRole)
                                .build();

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(200))
                                .andExpect(jsonPath("$.data.username").value("newuser"))
                                .andExpect(jsonPath("$.data.email").value("newuser@example.com"));
        }

        @Test
        @DisplayName("POST /api/v1/auth/register - Username Already Exists")
        void register_whenUsernameExists_shouldReturnBadRequest() throws Exception {
                RegisterRequest request = RegisterRequest.builder()
                                .username(existingUsername)
                                .email("newemail@example.com")
                                .password("passwordkuat456")
                                .role(userRole)
                                .build();

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value(400))
                                .andExpect(jsonPath("$.errors.username", hasSize(1)))
                                .andExpect(jsonPath("$.errors.username[0]").value("Username sudah terdaftar."));
        }

        @Test
        @DisplayName("POST /api/v1/auth/register - Email Already Exists")
        void register_whenEmailExists_shouldReturnBadRequest() throws Exception {
                RegisterRequest request = RegisterRequest.builder()
                                .username("anothernewuser")
                                .email(existingEmail)
                                .password("passwordkuat456")
                                .role(userRole)
                                .build();

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value(400))
                                .andExpect(jsonPath("$.errors.email", hasSize(1)))
                                .andExpect(jsonPath("$.errors.email[0]").value("Email sudah terdaftar."));
        }

        @Test
        @DisplayName("POST /api/v1/auth/register - Invalid Input Data")
        void register_whenInputInvalid_shouldReturnBadRequestWithValidationErrors() throws Exception {
                RegisterRequest request = RegisterRequest.builder()
                                .username("us")
                                .email("invalid-email")
                                .password("pass")
                                .role(userRole)
                                .build();

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value(400))
                                .andExpect(jsonPath("$.errors.username").exists())
                                .andExpect(jsonPath("$.errors.email").exists())
                                .andExpect(jsonPath("$.errors.password").exists());
        }

        @Test
        @DisplayName("POST /api/v1/auth/login - Success")
        void login_whenValidCredentials_shouldReturnOkAndToken() throws Exception {
                LoginRequest request = new LoginRequest(existingUsername, rawPassword);

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(200))
                                .andExpect(jsonPath("$.data.token").exists())
                                .andExpect(jsonPath("$.data.token").isString())
                                .andExpect(jsonPath("$.data.token").isNotEmpty());
        }

        @Test
        @DisplayName("POST /api/v1/auth/login - Invalid Password")
        void login_whenPasswordInvalid_shouldReturnUnauthorized() throws Exception {
                LoginRequest request = new LoginRequest(existingUsername, "wrongpassword");

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value(400))
                                .andExpect(jsonPath("$.errors").exists());
        }

        @Test
        @DisplayName("POST /api/v1/auth/login - User Not Found")
        void login_whenUserNotFound_shouldReturnNotFound() throws Exception {
                LoginRequest request = new LoginRequest("nonexistentuser", "password123");

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.code").value(401))
                                .andExpect(jsonPath("$.message").exists());
        }
}
