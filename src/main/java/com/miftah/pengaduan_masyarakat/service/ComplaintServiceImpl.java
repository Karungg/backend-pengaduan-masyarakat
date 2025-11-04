package com.miftah.pengaduan_masyarakat.service;

import com.miftah.pengaduan_masyarakat.dto.ComplaintRequest;
import com.miftah.pengaduan_masyarakat.dto.ComplaintResponse;
import com.miftah.pengaduan_masyarakat.enums.StatusEnum;
import com.miftah.pengaduan_masyarakat.exception.ResourceNotFoundException;
import com.miftah.pengaduan_masyarakat.exception.ValidationException;
import com.miftah.pengaduan_masyarakat.model.*;
import com.miftah.pengaduan_masyarakat.repository.AgencyRepository;
import com.miftah.pengaduan_masyarakat.repository.ComplaintRepository;
import com.miftah.pengaduan_masyarakat.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final AgencyRepository agencyRepository;
    private final MessageSource messageSource;

    @Transactional
    public ComplaintResponse createComplaint(ComplaintRequest request) {
        log.info("Attempting to create complaint for user ID: {}", request.getUserId());

        User user = findUserByIdOrThrow(request.getUserId());
        Agency agency = findAgencyByIdOrThrow(request.getAgencyId());

        Complaint complaint = new Complaint();
        complaint.setUser(user);
        complaint.setAgency(agency);
        complaint.setType(request.getType());
        complaint.setTitle(request.getTitle());
        complaint.setDescription(request.getDescription());
        complaint.setDate(request.getDate());
        complaint.setLocation(request.getLocation());
        complaint.setAttachmentUrl(request.getAttachmentUrl());
        complaint.setAspiration(request.getAspiration());

        complaint.setStatus(StatusEnum.PENDING);

        Complaint savedComplaint = complaintRepository.save(complaint);
        log.info("Complaint created successfully with ID: {}", savedComplaint.getId());

        return convertToComplaintResponse(savedComplaint);
    }

    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintById(UUID id) {
        log.debug("Fetching complaint with ID: {}", id);
        Complaint complaint = findComplaintByIdOrThrow(id);

        return convertToComplaintResponse(complaint);
    }

    @Transactional(readOnly = true)
    public List<ComplaintResponse> getAllComplaints() {
        log.debug("Fetching all complaints");

        List<Complaint> complaints = complaintRepository.findAll();

        return complaints.stream()
                .map(this::convertToComplaintResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ComplaintResponse updateComplaint(UUID id, ComplaintRequest request) {
        log.info("Attempting to update complaint with ID: {}", id);

        Complaint complaintToUpdate = findComplaintByIdOrThrow(id);
        Agency newAgency = findAgencyByIdOrThrow(request.getAgencyId());

        complaintToUpdate.setType(request.getType());
        complaintToUpdate.setTitle(request.getTitle());
        complaintToUpdate.setDescription(request.getDescription());
        complaintToUpdate.setDate(request.getDate());
        complaintToUpdate.setLocation(request.getLocation());
        complaintToUpdate.setAttachmentUrl(request.getAttachmentUrl());
        complaintToUpdate.setAspiration(request.getAspiration());
        complaintToUpdate.setAgency(newAgency);

        Complaint updatedComplaint = complaintRepository.save(complaintToUpdate);
        log.info("Complaint updated successfully with ID: {}", updatedComplaint.getId());

        return convertToComplaintResponse(updatedComplaint);
    }

    @Transactional
    public void deleteComplaint(UUID id) {
        log.info("Attempting to delete complaint with ID: {}", id);

        if (!complaintRepository.existsById(id)) {
            log.warn("Deletion failed. Complaint with ID {} not found.", id);
            throw createResourceNotFoundException("complaint.notfound.id", id);
        }

        complaintRepository.deleteById(id);
        log.info("Complaint deleted successfully with ID: {}", id);
    }

    private Complaint findComplaintByIdOrThrow(UUID id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> createResourceNotFoundException("complaint.notfound.id", id));
    }

    private User findUserByIdOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Invalid request. User (pelapor) with ID {} not found.", userId);
                    Locale locale = LocaleContextHolder.getLocale();
                    String message = messageSource.getMessage("user.notfound.id", new Object[] { userId },
                            "User (pelapor) tidak ditemukan.", locale);
                    return new ValidationException(message, Map.of("userId", List.of(message)));
                });
    }

    private Agency findAgencyByIdOrThrow(UUID agencyId) {
        return agencyRepository.findById(agencyId)
                .orElseThrow(() -> {
                    log.warn("Invalid request. Agency with ID {} not found.", agencyId);
                    Locale locale = LocaleContextHolder.getLocale();
                    String message = messageSource.getMessage("agency.notfound.id", new Object[] { agencyId },
                            "Agensi (instansi) tidak ditemukan.", locale);
                    return new ValidationException(message, Map.of("agencyId", List.of(message)));
                });
    }

    private ComplaintResponse convertToComplaintResponse(Complaint complaint) {
        User user = complaint.getUser();
        Agency agency = complaint.getAgency();

        ComplaintResponse response = new ComplaintResponse();
        response.setId(complaint.getId());
        response.setType(complaint.getType());
        response.setTitle(complaint.getTitle());
        response.setDescription(complaint.getDescription());
        response.setDate(complaint.getDate());
        response.setLocation(complaint.getLocation());
        response.setAttachmentUrl(complaint.getAttachmentUrl());
        response.setStatus(complaint.getStatus());
        response.setAspiration(complaint.getAspiration());
        response.setCreatedAt(complaint.getCreatedAt());
        response.setUpdatedAt(complaint.getUpdatedAt());

        if (user != null) {
            response.setUserId(user.getId());
            response.setUsername(user.getUsername());
        }
        if (agency != null) {
            response.setAgencyId(agency.getId());
            response.setAgencyName(agency.getName());
        }

        return response;
    }

    private ResourceNotFoundException createResourceNotFoundException(String messageKey, Object... args) {
        Locale currentLocale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(messageKey, args, "Resource not found", currentLocale);
        return new ResourceNotFoundException(message);
    }
}
