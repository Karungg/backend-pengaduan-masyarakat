package com.miftah.pengaduan_masyarakat.service;

import com.miftah.pengaduan_masyarakat.dto.UserRequest;
import com.miftah.pengaduan_masyarakat.dto.UserResponse;
import com.miftah.pengaduan_masyarakat.enums.RoleEnum;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse createUser(UserRequest request);

    UserResponse getUserById(UUID id);

    List<UserResponse> getAllUsers(RoleEnum role);

    UserResponse updateUser(UUID id, UserRequest request);

    void deleteUser(UUID id);
}