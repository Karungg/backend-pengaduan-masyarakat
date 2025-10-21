package com.miftah.pengaduan_masyarakat.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private UUID id;

    private String username;

    private String email;

    private Instant createdAt;

    private Instant updatedAt;
}
