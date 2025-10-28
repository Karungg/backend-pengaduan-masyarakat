package com.miftah.pengaduan_masyarakat.controller;

import com.miftah.pengaduan_masyarakat.dto.AgencyRequest;
import com.miftah.pengaduan_masyarakat.dto.AgencyResponse;
import com.miftah.pengaduan_masyarakat.dto.GenericResponse;
import com.miftah.pengaduan_masyarakat.service.AgencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agencies")
@Slf4j
@RequiredArgsConstructor
public class AgencyController {

    private final AgencyService agencyService;

    @PostMapping
    public ResponseEntity<GenericResponse<AgencyResponse>> createAgency(@Valid @RequestBody AgencyRequest request) {
        log.info("Received request to create agency with name: {}", request.getName());
        AgencyResponse createdAgency = agencyService.createAgency(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdAgency.getId())
                .toUri();

        GenericResponse<AgencyResponse> response = GenericResponse.created(createdAgency);
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse<AgencyResponse>> getAgencyById(@PathVariable UUID id) {
        log.info("Received request to fetch agency with ID: {}", id);
        AgencyResponse agency = agencyService.getAgencyById(id);
        GenericResponse<AgencyResponse> response = GenericResponse.ok(agency);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<GenericResponse<List<AgencyResponse>>> getAllAgencies() {
        log.info("Received request to fetch all agencies");
        List<AgencyResponse> agencies = agencyService.getAllAgencies();
        GenericResponse<List<AgencyResponse>> response = GenericResponse.ok(agencies);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenericResponse<AgencyResponse>> updateAgency(
            @PathVariable UUID id,
            @Valid @RequestBody AgencyRequest request) {
        log.info("Received request to update agency with ID: {}", id);
        AgencyResponse updatedAgency = agencyService.updateAgencies(id, request);
        GenericResponse<AgencyResponse> response = GenericResponse.ok(updatedAgency);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAgency(@PathVariable UUID id) {
        log.info("Received request to delete agency with ID: {}", id);
        agencyService.deleteAgency(id);
        return ResponseEntity.noContent().build();
    }
}