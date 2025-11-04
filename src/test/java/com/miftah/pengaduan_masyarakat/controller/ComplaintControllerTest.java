package com.miftah.pengaduan_masyarakat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miftah.pengaduan_masyarakat.dto.ComplaintRequest;
import com.miftah.pengaduan_masyarakat.enums.RoleEnum;
import com.miftah.pengaduan_masyarakat.enums.StatusEnum;
import com.miftah.pengaduan_masyarakat.enums.TypeEnum;
import com.miftah.pengaduan_masyarakat.enums.VisibilityEnum;
import com.miftah.pengaduan_masyarakat.model.*;
import com.miftah.pengaduan_masyarakat.repository.AgencyRepository;
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

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(roles = "ADMIN")
class ComplaintControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private ComplaintRepository complaintRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private AgencyRepository agencyRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        private User reportingUser;
        private Agency targetAgency;
        private Complaint existingComplaint;

        @BeforeEach
        void setUp() {
                reportingUser = new User();
                reportingUser.setUsername("pelapor_test");
                reportingUser.setEmail("pelapor@example.com");
                reportingUser.setPassword(passwordEncoder.encode("password123"));
                reportingUser.setRole(RoleEnum.USER);
                userRepository.save(reportingUser);

                User agencyUser = new User();
                agencyUser.setUsername("agensi_test");
                agencyUser.setEmail("agensi@example.com");
                agencyUser.setPassword(passwordEncoder.encode("password123"));
                agencyUser.setRole(RoleEnum.AGENCY);
                userRepository.save(agencyUser);

                targetAgency = new Agency();
                targetAgency.setName("Dinas Test");
                targetAgency.setAddress("Jl. Test No. 1");
                targetAgency.setPhone("08123456789");
                targetAgency.setUser(agencyUser);
                agencyRepository.save(targetAgency);

                existingComplaint = new Complaint();
                existingComplaint.setUser(reportingUser);
                existingComplaint.setAgency(targetAgency);
                existingComplaint.setType(TypeEnum.COMPLAINT);
                existingComplaint.setVisibility(VisibilityEnum.PUBLIC);
                existingComplaint.setTitle("Jalan Rusak");
                existingComplaint.setDescription("Jalan di depan rumah rusak berat.");
                existingComplaint.setDate(Instant.now().minusSeconds(86400));
                existingComplaint.setLocation("Bogor");
                existingComplaint.setStatus(StatusEnum.PENDING);
                complaintRepository.save(existingComplaint);
        }

        @Test
        @DisplayName("POST /api/v1/complaints - Sukses")
        void createComplaint_whenValidRequest_shouldReturn201() throws Exception {
                ComplaintRequest request = new ComplaintRequest(
                                TypeEnum.ASPIRATION,
                                VisibilityEnum.PUBLIC,
                                "Usulan Perbaikan",
                                null,
                                Instant.now(),
                                "Taman Kota",
                                null,
                                "Mohon ditambahkan lebih banyak lampu taman.",
                                reportingUser.getId(),
                                targetAgency.getId());

                mockMvc.perform(post("/api/v1/complaints")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(header().string("Location", containsString("/api/v1/complaints/")))
                                .andExpect(jsonPath("$.code").value(201))
                                .andExpect(jsonPath("$.data.title").value("Usulan Perbaikan"))
                                .andExpect(jsonPath("$.data.status").value("PENDING"));
        }

        @Test
        @DisplayName("POST /api/v1/complaints - Error Validasi DTO")
        void createComplaint_whenInvalidRequest_shouldReturn400() throws Exception {
                ComplaintRequest request = new ComplaintRequest(
                                null,
                                null,
                                null, null, null,
                                "",
                                null, null,
                                null,
                                null);

                mockMvc.perform(post("/api/v1/complaints")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value(400))
                                .andExpect(jsonPath("$.errors.type").exists())
                                .andExpect(jsonPath("$.errors.date").exists())
                                .andExpect(jsonPath("$.errors.location").exists())
                                .andExpect(jsonPath("$.errors.userId").exists())
                                .andExpect(jsonPath("$.errors.agencyId").exists());
        }

        @Test
        @DisplayName("POST /api/v1/complaints - Error User Tidak Ditemukan")
        void createComplaint_whenUserNotFound_shouldReturn400() throws Exception {
                UUID randomUserId = UUID.randomUUID();
                ComplaintRequest request = new ComplaintRequest(
                                TypeEnum.COMPLAINT,
                                VisibilityEnum.PRIVATE,
                                "Judul",
                                "Deskripsi",
                                Instant.now(),
                                "Lokasi",
                                null, null,
                                randomUserId,
                                targetAgency.getId());

                mockMvc.perform(post("/api/v1/complaints")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value(400))
                                .andExpect(jsonPath("$.errors.userId").exists());
        }

        @Test
        @DisplayName("GET /api/v1/complaints - Sukses")
        void getAllComplaints_shouldReturnList() throws Exception {
                mockMvc.perform(get("/api/v1/complaints"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(200))
                                .andExpect(jsonPath("$.data", hasSize(1)))
                                .andExpect(jsonPath("$.data[0].id").value(existingComplaint.getId().toString()));
        }

        @Test
        @DisplayName("GET /api/v1/complaints/{id} - Sukses")
        void getComplaintById_whenExists_shouldReturnData() throws Exception {
                mockMvc.perform(get("/api/v1/complaints/{id}", existingComplaint.getId()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(200))
                                .andExpect(jsonPath("$.data.id").value(existingComplaint.getId().toString()))
                                .andExpect(jsonPath("$.data.title").value("Jalan Rusak"))
                                .andExpect(jsonPath("$.data.username").value(reportingUser.getUsername()))
                                .andExpect(jsonPath("$.data.agencyName").value(targetAgency.getName()));
        }

        @Test
        @DisplayName("GET /api/v1/complaints/{id} - Tidak Ditemukan")
        void getComplaintById_whenNotFound_shouldReturn404() throws Exception {
                mockMvc.perform(get("/api/v1/complaints/{id}", UUID.randomUUID()))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code").value(404));
        }

        @Test
        @DisplayName("PUT /api/v1/complaints/{id} - Sukses")
        void updateComplaint_whenValid_shouldReturn200() throws Exception {
                ComplaintRequest request = new ComplaintRequest(
                                TypeEnum.COMPLAINT,
                                VisibilityEnum.PRIVATE,
                                "UPDATE: Jalan Rusak Parah",
                                "Deskripsi update",
                                existingComplaint.getDate(),
                                "Bogor Barat",
                                null, null,
                                reportingUser.getId(),
                                targetAgency.getId());

                mockMvc.perform(put("/api/v1/complaints/{id}", existingComplaint.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(200))
                                .andExpect(jsonPath("$.data.title").value("UPDATE: Jalan Rusak Parah"))
                                .andExpect(jsonPath("$.data.location").value("Bogor Barat"));
        }

        @Test
        @DisplayName("DELETE /api/v1/complaints/{id} - Sukses")
        void deleteComplaint_whenExists_shouldReturn204() throws Exception {
                mockMvc.perform(delete("/api/v1/complaints/{id}", existingComplaint.getId()))
                                .andExpect(status().isNoContent());

                assertFalse(complaintRepository.existsById(existingComplaint.getId()));
        }

        @Test
        @DisplayName("DELETE /api/v1/complaints/{id} - Tidak Ditemukan")
        void deleteComplaint_whenNotFound_shouldReturn404() throws Exception {
                mockMvc.perform(delete("/api/v1/complaints/{id}", UUID.randomUUID()))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code").value(404));
        }
}
