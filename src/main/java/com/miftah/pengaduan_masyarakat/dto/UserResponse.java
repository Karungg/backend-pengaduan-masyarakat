package com.miftah.pengaduan_masyarakat.dto;

import java.time.Instant;
import java.util.UUID;

import com.miftah.pengaduan_masyarakat.enums.RoleEnum;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;

    private String username;

    private String email;

    private RoleEnum role;

    private Instant createdAt;

    private Instant updatedAt;
}
