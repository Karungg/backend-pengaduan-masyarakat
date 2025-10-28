package com.miftah.pengaduan_masyarakat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miftah.pengaduan_masyarakat.dto.AgencyRequest;
import com.miftah.pengaduan_masyarakat.dto.UserRequest;
import com.miftah.pengaduan_masyarakat.enums.RoleEnum;
import com.miftah.pengaduan_masyarakat.model.Agency;
import com.miftah.pengaduan_masyarakat.model.User;
import com.miftah.pengaduan_masyarakat.repository.AgencyRepository;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AgencyControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private AgencyRepository agencyRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        private Agency existingAgency;
        private User agencyUser;

        @BeforeEach
        void setUp() {
                agencyUser = new User();
                agencyUser.setUsername("dinas_kesehatan");
                agencyUser.setEmail("dinkes@bogor.go.id");
                agencyUser.setPassword(passwordEncoder.encode("password123"));
                agencyUser.setRole(RoleEnum.AGENCY);
                userRepository.save(agencyUser);

                existingAgency = new Agency();
                existingAgency.setName("Dinas Kesehatan Kota Bogor");
                existingAgency.setAddress("Jl. Kesehatan No. 1, Bogor Tengah");
                existingAgency.setPhone("02518321234");
                existingAgency.setUser(agencyUser);
                agencyRepository.save(existingAgency);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("POST /api/v1/agencies - Success")
        void createAgency_whenValidRequest_shouldReturn201AndAgencyData() throws Exception {
                UserRequest newUserRequest = new UserRequest("dinas_sosial", "dinsos@bogor.go.id", "PassDinsos456!",
                                RoleEnum.AGENCY);
                AgencyRequest newAgencyRequest = new AgencyRequest("Dinas Sosial Kota Bogor", "Jl. Ir. H. Juanda No.10",
                                "02518327888", newUserRequest);

                mockMvc.perform(post("/api/v1/agencies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newAgencyRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(header().string("Location", containsString("/api/v1/agencies/")))
                                .andExpect(jsonPath("$.code").value(201))
                                .andExpect(jsonPath("$.data.name").value("Dinas Sosial Kota Bogor"))
                                .andExpect(jsonPath("$.data.phone").value("02518327888"))
                                .andExpect(jsonPath("$.data.userId")
                                                .exists());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("POST /api/v1/agencies - Invalid Data")
        void createAgency_whenInvalidData_shouldReturn400() throws Exception {
                UserRequest invalidUserRequest = new UserRequest("di", "dinsos@", "pass", null);
                AgencyRequest invalidAgencyRequest = new AgencyRequest("", "alamat", "123", invalidUserRequest);

                mockMvc.perform(post("/api/v1/agencies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidAgencyRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value(400))
                                .andExpect(jsonPath("$.errors.name").exists())
                                .andExpect(jsonPath("$.errors.phone").exists())
                                .andExpect(jsonPath("$.errors['user.username']").exists())
                                .andExpect(jsonPath("$.errors['user.email']").exists())
                                .andExpect(jsonPath("$.errors['user.password']").exists())
                                .andExpect(jsonPath("$.errors['user.role']").exists());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("POST /api/v1/agencies - Forbidden Access")
        void createAgency_whenUserRole_shouldReturn403() throws Exception {
                UserRequest newUserRequest = new UserRequest("test_user_forbidden", "forbidden@example.com",
                                "Password123!",
                                RoleEnum.AGENCY);
                AgencyRequest newAgencyRequest = new AgencyRequest("Forbidden Agency", "Some Address", "0811111111",
                                newUserRequest);

                mockMvc.perform(post("/api/v1/agencies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newAgencyRequest)))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("GET /api/v1/agencies/{id} - Success")
        void getAgencyById_whenExists_shouldReturnAgencyData() throws Exception {
                mockMvc.perform(get("/api/v1/agencies/{id}", existingAgency.getId()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(200))
                                .andExpect(jsonPath("$.data.id").value(existingAgency.getId().toString()))
                                .andExpect(jsonPath("$.data.name").value("Dinas Kesehatan Kota Bogor"))
                                .andExpect(jsonPath("$.data.userId").value(agencyUser.getId().toString()));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("GET /api/v1/agencies/{id} - Not Found")
        void getAgencyById_whenNotFound_shouldReturn404() throws Exception {
                UUID randomId = UUID.randomUUID();
                mockMvc.perform(get("/api/v1/agencies/{id}", randomId))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code").value(404));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("GET /api/v1/agencies - Success")
        void getAllAgencies_shouldReturnListOfAgencies() throws Exception {

                User user2 = new User();
                user2.setUsername("dinas_pendidikan");
                user2.setEmail("disdik@bogor.go.id");
                user2.setPassword(passwordEncoder.encode("password456"));
                user2.setRole(RoleEnum.AGENCY);
                userRepository.save(user2);
                Agency agency2 = new Agency();
                agency2.setName("Dinas Pendidikan Kota Bogor");
                agency2.setAddress("Jl. Pajajaran No. 121");
                agency2.setPhone("02518321773");
                agency2.setUser(user2);
                agencyRepository.save(agency2);

                mockMvc.perform(get("/api/v1/agencies"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(200))
                                .andExpect(jsonPath("$.data", hasSize(2)))
                                .andExpect(jsonPath("$.data[0].name").exists())
                                .andExpect(jsonPath("$.data[1].name").exists());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("PUT /api/v1/agencies/{id} - Success")
        void updateAgency_whenValidRequest_shouldReturnUpdatedData() throws Exception {

                UserRequest userUpdateRequest = new UserRequest("dinkes_updated", "dinkes.baru@bogor.go.id",
                                "PasswordBaru789!",
                                RoleEnum.AGENCY);
                AgencyRequest updateRequest = new AgencyRequest("Dinas Kesehatan V2", "Jl. Kesehatan No. 2",
                                "02518333444",
                                userUpdateRequest);

                mockMvc.perform(put("/api/v1/agencies/{id}", existingAgency.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(200))
                                .andExpect(jsonPath("$.data.name").value("Dinas Kesehatan V2"))
                                .andExpect(jsonPath("$.data.phone").value("02518333444"))
                                .andExpect(jsonPath("$.data.userId").value(agencyUser.getId().toString()));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("PUT /api/v1/agencies/{id} - Not Found")
        void updateAgency_whenNotFound_shouldReturn404() throws Exception {
                UUID randomId = UUID.randomUUID();
                UserRequest userUpdateRequest = new UserRequest("update_fail", "fail@example.com", "PasswordFail1!",
                                RoleEnum.AGENCY);
                AgencyRequest updateRequest = new AgencyRequest("Update Fail", "Fail Address", "0800000000",
                                userUpdateRequest);

                mockMvc.perform(put("/api/v1/agencies/{id}", randomId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code").value(404));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("DELETE /api/v1/agencies/{id} - Success")
        void deleteAgency_whenExists_shouldReturn204() throws Exception {
                mockMvc.perform(delete("/api/v1/agencies/{id}", existingAgency.getId()))
                                .andExpect(status().isNoContent());

                assertFalse(agencyRepository.existsById(existingAgency.getId()));
                assertFalse(userRepository.existsById(agencyUser.getId()));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("DELETE /api/v1/agencies/{id} - Not Found")
        void deleteAgency_whenNotFound_shouldReturn404() throws Exception {
                UUID randomId = UUID.randomUUID();
                mockMvc.perform(delete("/api/v1/agencies/{id}", randomId))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code").value(404));
        }
}
