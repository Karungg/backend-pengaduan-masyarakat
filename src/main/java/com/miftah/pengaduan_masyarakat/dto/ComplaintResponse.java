package com.miftah.pengaduan_masyarakat.dto;

import java.time.Instant;
import java.util.UUID;

import com.miftah.pengaduan_masyarakat.enums.StatusEnum;
import com.miftah.pengaduan_masyarakat.enums.TypeEnum;
import com.miftah.pengaduan_masyarakat.enums.VisibilityEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintResponse {
    private UUID id;

    private TypeEnum type;

    private VisibilityEnum visibility;

    private String title;

    private String description;

    private Instant date;

    private String location;

    private String attachmentUrl;

    private StatusEnum status;

    private String aspiration;

    private Instant createdAt;

    private Instant updatedAt;

    private UUID userId;

    private String username;

    private UUID agencyId;

    private String agencyName;

}
