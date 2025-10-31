package com.miftah.pengaduan_masyarakat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgencyResponse {

    private UUID id;

    private String name;

    private String address;

    private String phone;

    private UserResponse user;

    private Instant createdAt;

    private Instant updatedAt;

}