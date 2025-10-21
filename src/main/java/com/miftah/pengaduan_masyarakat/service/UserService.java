package com.miftah.pengaduan_masyarakat.service;

import com.miftah.pengaduan_masyarakat.dto.UserRequest;
import com.miftah.pengaduan_masyarakat.dto.UserResponse;
import java.util.List;
import java.util.UUID;

public interface UserService{

    UserResponse createUser(UserRequest request);

    UserResponse getUserById(UUID id);

    List<UserResponse> getAllUsers();

    UserResponse updateUser(UUID id, UserRequest request);

    void deleteUser(UUID id);
}