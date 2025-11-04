package com.miftah.pengaduan_masyarakat.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.miftah.pengaduan_masyarakat.dto.ComplaintRequest;
import com.miftah.pengaduan_masyarakat.dto.ComplaintResponse;
import com.miftah.pengaduan_masyarakat.dto.GenericResponse;
import com.miftah.pengaduan_masyarakat.service.ComplaintService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/complaints")
@Slf4j
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @PostMapping
    public ResponseEntity<GenericResponse<ComplaintResponse>> createComplaint(@Valid @RequestBody ComplaintRequest request) {
        log.info("Received request to create complaint with user_id: {}", request.getUserId());
        ComplaintResponse createdComplaint = complaintService.createComplaint(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdComplaint.getId())
                .toUri();

        return ResponseEntity.created(location)
                .body(GenericResponse.created(createdComplaint));
    }

    @GetMapping
    public ResponseEntity<GenericResponse<List<ComplaintResponse>>> getAllComplaints() {
        log.info("Received request to fetch all complaints");

        List<ComplaintResponse> complaints = complaintService.getAllComplaints();

        return ResponseEntity.ok(GenericResponse.ok(complaints));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse<ComplaintResponse>> getComplaintById(@PathVariable UUID id) {
        log.info("Received request to fetch complaint with ID: {}", id);

        ComplaintResponse complaint = complaintService.getComplaintById(id);

        return ResponseEntity.ok(GenericResponse.ok(complaint));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenericResponse<ComplaintResponse>> updateComplaint(@PathVariable UUID id,
            @Valid @RequestBody ComplaintRequest request) {
        log.info("Received request to update complaint with ID: {}", id);

        ComplaintResponse updatedComplaint = complaintService.updateComplaint(id, request);

        return ResponseEntity.ok(GenericResponse.ok(updatedComplaint));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponse<Void>> deleteComplaint(@PathVariable UUID id) {
        log.info("Received request to delete complaint with ID: {}", id);

        complaintService.deleteComplaint(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(GenericResponse.noContent());
    }
}
