package com.miftah.pengaduan_masyarakat.dto;

import java.time.Instant;
import java.util.UUID;

import com.miftah.pengaduan_masyarakat.enums.TypeEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintRequest {

    @NotNull(message = "{complaint.type.notnull}")
    private TypeEnum type;

    @Size(max = 255, message = "{complaint.title.size}")
    private String title;

    @Size(max = 4000, message = "{complaint.description.size}")
    private String description;

    @NotNull(message = "{complaint.date.notnull}")
    @PastOrPresent(message = "{complaint.date.pastorpresent}")
    private Instant date;

    @NotBlank(message = "{complaint.location.notblank}")
    @Size(max = 255, message = "{complaint.location.size}")
    private String location;

    @Size(max = 1024, message = "{complaint.attachmenturl.size}")
    private String attachmentUrl;

    @Size(max = 4000, message = "{complaint.aspiration.size}")
    private String aspiration;

    @NotNull(message = "{complaint.userid.notnull}")
    private UUID userId;

    @NotNull(message = "{complaint.agencyId.notnull}")
    private UUID agencyId;
}
