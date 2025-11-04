package com.miftah.pengaduan_masyarakat.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {

    private UUID id;

    private String name;

    private String description;

    private Instant createdAt;

    private Instant updatedAt;

}
