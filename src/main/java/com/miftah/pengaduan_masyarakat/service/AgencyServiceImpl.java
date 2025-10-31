package com.miftah.pengaduan_masyarakat.service;

import com.miftah.pengaduan_masyarakat.dto.AgencyRequest;
import com.miftah.pengaduan_masyarakat.dto.AgencyResponse;
import com.miftah.pengaduan_masyarakat.dto.UserResponse;
import com.miftah.pengaduan_masyarakat.enums.RoleEnum;
import com.miftah.pengaduan_masyarakat.exception.ResourceNotFoundException;
import com.miftah.pengaduan_masyarakat.exception.ValidationException;
import com.miftah.pengaduan_masyarakat.model.Agency;
import com.miftah.pengaduan_masyarakat.model.User;
import com.miftah.pengaduan_masyarakat.repository.AgencyRepository;
import com.miftah.pengaduan_masyarakat.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AgencyServiceImpl implements AgencyService {

    private final AgencyRepository agencyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;

    @Transactional
    public AgencyResponse createAgency(AgencyRequest request) {
        log.info("Attempting to create new agency with name: {}", request.getName());
        Locale currentLocale = LocaleContextHolder.getLocale();
        Map<String, List<String>> errors = new HashMap<>();

        validateAgencyUniqueness(request.getPhone(), null, errors, currentLocale);
        validateUserUniqueness(request.getUser().getUsername(), request.getUser().getEmail(), null, errors,
                currentLocale);

        if (!errors.isEmpty()) {
            log.warn("Agency creation failed due to validation errors: {}", errors);
            throw new ValidationException("Validation failed", errors);
        }

        User user = new User();
        user.setUsername(request.getUser().getUsername());
        user.setEmail(request.getUser().getEmail());
        user.setPassword(passwordEncoder.encode(request.getUser().getPassword()));
        user.setRole(RoleEnum.AGENCY);
        User savedUser = userRepository.save(user);
        log.info("Associated user created with ID: {}", savedUser.getId());

        Agency agency = new Agency();
        agency.setName(request.getName());
        agency.setAddress(request.getAddress());
        agency.setPhone(request.getPhone());
        agency.setUser(savedUser);

        Agency savedAgency = agencyRepository.save(agency);
        log.info("Agency created successfully with ID: {}", savedAgency.getId());

        return convertToAgencyResponse(savedAgency);
    }

    @Transactional(readOnly = true)
    public AgencyResponse getAgencyById(UUID id) {
        log.debug("Fetching agency with ID: {}", id);
        Agency agency = findAgencyByIdOrThrow(id);
        return convertToAgencyResponse(agency);
    }

    @Transactional(readOnly = true)
    public List<AgencyResponse> getAllAgencies() {
        log.debug("Fetching all agencies");
        return agencyRepository.findAll().stream()
                .map(this::convertToAgencyResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AgencyResponse updateAgencies(UUID id, AgencyRequest request) {
        log.info("Attempting to update agency with ID: {}", id);
        Locale currentLocale = LocaleContextHolder.getLocale();
        Map<String, List<String>> errors = new HashMap<>();

        Agency agencyToUpdate = findAgencyByIdOrThrow(id);
        User userToUpdate = agencyToUpdate.getUser();

        validateAgencyUniqueness(request.getPhone(), id, errors, currentLocale);
        validateUserUniqueness(request.getUser().getUsername(), request.getUser().getEmail(), userToUpdate.getId(),
                errors, currentLocale);

        if (!errors.isEmpty()) {
            log.warn("Agency update failed for ID {} due to validation errors: {}", id, errors);
            throw new ValidationException("Validation failed", errors);
        }

        agencyToUpdate.setName(request.getName());
        agencyToUpdate.setAddress(request.getAddress());
        agencyToUpdate.setPhone(request.getPhone());

        boolean userChanged = false;
        if (request.getUser().getEmail() != null && !request.getUser().getEmail().equals(userToUpdate.getEmail())) {
            userToUpdate.setEmail(request.getUser().getEmail());
            userChanged = true;
        }
        if (request.getUser().getPassword() != null && !request.getUser().getPassword().isBlank()) {
            userToUpdate.setPassword(passwordEncoder.encode(request.getUser().getPassword()));
            userChanged = true;
        }
        if (request.getUser().getUsername() != null
                && !request.getUser().getUsername().equals(userToUpdate.getUsername())) {
            userToUpdate.setUsername(request.getUser().getUsername());
            userChanged = true;
        }

        if (userChanged) {
            userRepository.save(userToUpdate);
        }
        Agency updatedAgency = agencyRepository.save(agencyToUpdate);
        log.info("Agency with ID {} updated successfully.", updatedAgency.getId());

        return convertToAgencyResponse(updatedAgency);
    }

    @Transactional
    public void deleteAgency(UUID id) {
        log.info("Attempting to delete agency with ID: {}", id);
        if (!agencyRepository.existsById(id)) {
            log.warn("Deletion failed. Agency with ID {} not found.", id);
            throw new ResourceNotFoundException("Agency not found with id: " + id);
        }
        agencyRepository.deleteById(id);
        log.info("Agency with ID {} deleted successfully.", id);
    }

    private Agency findAgencyByIdOrThrow(UUID id) {
        return agencyRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Agency with ID {} not found.", id);
                    return new ResourceNotFoundException("Agency not found with id: " + id);
                });
    }

    private AgencyResponse convertToAgencyResponse(Agency agency) {
        return new AgencyResponse(
                agency.getId(),
                agency.getName(),
                agency.getAddress(),
                agency.getPhone(),
                convertToUserResponse(agency.getUser()),
                agency.getCreatedAt(),
                agency.getUpdatedAt());
    }

    private UserResponse convertToUserResponse(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    private void validateAgencyUniqueness(String phone, UUID currentAgencyId, Map<String, List<String>> errors,
            Locale locale) {
        Optional<Agency> existingAgencyByPhone = agencyRepository.findByPhone(phone);
        if (existingAgencyByPhone.isPresent()
                && (currentAgencyId == null || !existingAgencyByPhone.get().getId().equals(currentAgencyId))) {
            String message = messageSource.getMessage("agency.phone.unique", null, "Nomor telepon sudah terdaftar.",
                    locale);
            errors.computeIfAbsent("phone", k -> new ArrayList<>()).add(message);
        }
    }

    private void validateUserUniqueness(String username, String email, UUID currentUserId,
            Map<String, List<String>> errors, Locale locale) {
        Optional<User> existingUserByUsername = userRepository.findByUsername(username);
        if (existingUserByUsername.isPresent()
                && (currentUserId == null || !existingUserByUsername.get().getId().equals(currentUserId))) {
            String message = messageSource.getMessage("user.username.unique", null, "Username sudah terdaftar.",
                    locale);
            errors.computeIfAbsent("user.username", k -> new ArrayList<>()).add(message);
        }

        Optional<User> existingUserByEmail = userRepository.findByEmail(email);
        if (existingUserByEmail.isPresent()
                && (currentUserId == null || !existingUserByEmail.get().getId().equals(currentUserId))) {
            String message = messageSource.getMessage("user.email.unique", null, "Email sudah terdaftar.", locale);
            errors.computeIfAbsent("user.email", k -> new ArrayList<>()).add(message);
        }
    }
}
