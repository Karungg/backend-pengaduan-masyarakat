package com.miftah.pengaduan_masyarakat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miftah.pengaduan_masyarakat.dto.CategoryRequest;
import com.miftah.pengaduan_masyarakat.enums.RoleEnum;
import com.miftah.pengaduan_masyarakat.model.Category;
import com.miftah.pengaduan_masyarakat.model.User;
import com.miftah.pengaduan_masyarakat.repository.CategoryRepository;
import com.miftah.pengaduan_masyarakat.repository.ComplaintRepository;
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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(roles = "ADMIN")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Category existingCategory;
    private User testUser;

    @BeforeEach
    void setUp() {
        existingCategory = new Category();
        existingCategory.setName("Infrastructure");
        existingCategory.setDescription("Related to roads, bridges, and public facilities.");
        categoryRepository.save(existingCategory);

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole(RoleEnum.USER);
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("POST /api/v1/categories - Success")
    void createCategory_whenValid_shouldReturn201() throws Exception {
        CategoryRequest request = new CategoryRequest("Health", "Related to hospital and clinic services.");

        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/categories/")))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.data.name").value("Health"))
                .andExpect(jsonPath("$.data.description").value("Related to hospital and clinic services."));
    }

    @Test
    @DisplayName("POST /api/v1/categories - Failure (DTO Validation - Name Blank)")
    void createCategory_whenNameIsBlank_shouldReturn400() throws Exception {
        CategoryRequest request = new CategoryRequest(null, "Description.");

        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    @DisplayName("POST /api/v1/categories - Failure (Business Logic - Duplicate Name)")
    void createCategory_whenNameExists_shouldReturn400() throws Exception {
        CategoryRequest request = new CategoryRequest("Infrastructure", "Duplicate description.");

        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} - Success")
    void getCategoryById_whenExists_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/categories/{id}", existingCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(existingCategory.getId().toString()))
                .andExpect(jsonPath("$.data.name").value("Infrastructure"));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} - Failure (Not Found)")
    void getCategoryById_whenNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/categories/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("GET /api/v1/categories - Success")
    void getAllCategories_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name").value("Infrastructure"));
    }

    @Test
    @DisplayName("PUT /api/v1/categories/{id} - Success")
    void updateCategory_whenValid_shouldReturn200() throws Exception {
        CategoryRequest request = new CategoryRequest("Infrastructure V2", "New description.");

        mockMvc.perform(put("/api/v1/categories/{id}", existingCategory.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("Infrastructure V2"))
                .andExpect(jsonPath("$.data.description").value("New description."));
    }

    @Test
    @DisplayName("PUT /api/v1/categories/{id} - Failure (Duplicate Name)")
    void updateCategory_whenNameExists_shouldReturn400() throws Exception {
        Category otherCategory = new Category();
        otherCategory.setName("Education");
        categoryRepository.save(otherCategory);

        CategoryRequest request = new CategoryRequest("Education", "Description.");

        mockMvc.perform(put("/api/v1/categories/{id}", existingCategory.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    @DisplayName("PUT /api/v1/categories/{id} - Failure (Not Found)")
    void updateCategory_whenNotFound_shouldReturn404() throws Exception {
        CategoryRequest request = new CategoryRequest("Update Fail", "Description.");

        mockMvc.perform(put("/api/v1/categories/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} - Success")
    void deleteCategory_whenExistsAndNotInUse_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/categories/{id}", existingCategory.getId()))
                .andExpect(status().isNoContent());

        assertFalse(categoryRepository.existsById(existingCategory.getId()));
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} - Failure (Not Found)")
    void deleteCategory_whenNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/v1/categories/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }
}