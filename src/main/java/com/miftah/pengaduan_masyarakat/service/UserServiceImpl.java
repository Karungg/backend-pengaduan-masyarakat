package com.miftah.pengaduan_masyarakat.service;

import com.miftah.pengaduan_masyarakat.dto.UserRequest;
import com.miftah.pengaduan_masyarakat.dto.UserResponse;
import com.miftah.pengaduan_masyarakat.enums.RoleEnum;
import com.miftah.pengaduan_masyarakat.exception.ResourceNotFoundException;
import com.miftah.pengaduan_masyarakat.exception.ValidationException;
import com.miftah.pengaduan_masyarakat.model.User;
import com.miftah.pengaduan_masyarakat.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;

    @Transactional
    public UserResponse createUser(UserRequest request) {
        log.info("Attempting to create new user with username: {}", request.getUsername());
        validateUserUniqueness(request.getUsername(), request.getEmail(), null);

        RoleEnum role = determineRoleFromRequest(request);

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        User savedUser = userRepository.save(user);
        log.info("Successfully created user with ID: {}", savedUser.getId());

        return convertToUserResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        log.debug("Fetching user by ID: {}", id);
        User user = findUserByIdOrThrow(id);
        return convertToUserResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(RoleEnum role) {
        log.debug("Fetching all users with role: {}", role);

        List<User> users;

        if (role != null) {
            users = userRepository.findByRole(role);
        } else {
            users = userRepository.findAll();
        }

        return users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateUser(UUID id, UserRequest request) {
        log.info("Attempting to update user with ID: {}", id);
        User userToUpdate = findUserByIdOrThrow(id);

        validateUserUniqueness(request.getUsername(), request.getEmail(), id);

        boolean changed = false;

        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equals(userToUpdate.getEmail())) {
            userToUpdate.setEmail(request.getEmail());
            changed = true;
            log.debug("Updating email for user ID: {}", id);
        }

        if (request.getUsername() != null && !request.getUsername().isBlank()
                && !request.getUsername().equals(userToUpdate.getUsername())) {
            userToUpdate.setUsername(request.getUsername());
            changed = true;
            log.debug("Updating username for user ID: {}", id);
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            userToUpdate.setPassword(passwordEncoder.encode(request.getPassword()));
            changed = true;
            log.debug("Updating password for user ID: {}", id);
        }

        RoleEnum requestedRole = determineRoleFromRequest(request);
        if (requestedRole != null && !requestedRole.equals(userToUpdate.getRole())) {
            userToUpdate.setRole(requestedRole);
            changed = true;
            log.debug("Updating role for user ID: {}", id);
        }

        if (changed) {
            User updatedUser = userRepository.save(userToUpdate);
            log.info("Successfully updated user with ID: {}", updatedUser.getId());
            return convertToUserResponse(updatedUser);
        } else {
            log.info("No changes detected for user with ID: {}", id);
            return convertToUserResponse(userToUpdate);
        }
    }

    @Transactional
    public void deleteUser(UUID id) {
        log.info("Attempting to delete user with ID: {}", id);
        if (!userRepository.existsById(id)) {
            log.warn("Deletion failed. User with ID {} not found.", id);
            throw createResourceNotFoundException("user.notfound.id", id);
        }
        userRepository.deleteById(id);
        log.info("Successfully deleted user with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        log.debug("Attempting to load user by username/email: {}", usernameOrEmail);

        return userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> {
                    log.warn("Authentication failed: User '{}' not found.", usernameOrEmail);

                    Locale currentLocale = LocaleContextHolder.getLocale();
                    String message = messageSource.getMessage("error.auth.usernotfound",
                            new Object[] { usernameOrEmail }, "User not found", currentLocale);
                    return new UsernameNotFoundException(message);
                });
    }

    private User findUserByIdOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> createResourceNotFoundException("user.notfound.id", id));
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

    private void validateUserUniqueness(String username, String email, UUID currentUserId) {
        Locale currentLocale = LocaleContextHolder.getLocale();
        Map<String, List<String>> errors = new HashMap<>();

        Optional<User> existingUserByUsername = userRepository.findByUsername(username);
        if (existingUserByUsername.isPresent()
                && (currentUserId == null || !existingUserByUsername.get().getId().equals(currentUserId))) {
            String message = messageSource.getMessage("user.username.unique", null, "Username sudah terdaftar.",
                    currentLocale);
            log.warn("Validation failed: {}", message);
            errors.computeIfAbsent("username", k -> new ArrayList<>()).add(message);
        }

        Optional<User> existingUserByEmail = userRepository.findByEmail(email);
        if (existingUserByEmail.isPresent()
                && (currentUserId == null || !existingUserByEmail.get().getId().equals(currentUserId))) {
            String message = messageSource.getMessage("user.email.unique", null, "Email sudah terdaftar.",
                    currentLocale);
            log.warn("Validation failed: {}", message);
            errors.computeIfAbsent("email", k -> new ArrayList<>()).add(message);
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
    }

    private ResourceNotFoundException createResourceNotFoundException(String messageKey, Object... args) {
        Locale currentLocale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(messageKey, args, "Resource not found", currentLocale);
        return new ResourceNotFoundException(message);
    }

    private RoleEnum determineRoleFromRequest(UserRequest request) {

        if (request.getRole() == null) {

            log.warn("Role not provided in request for user {}, defaulting to USER.", request.getUsername());
            return RoleEnum.USER;
        }
        return request.getRole();
    }
}
