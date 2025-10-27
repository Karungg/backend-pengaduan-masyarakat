package com.miftah.pengaduan_masyarakat.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AgencyRequest {

    @NotBlank(message = "{agency.name.notblank}")
    @Size(min = 3, max = 255, message = "{agency.name.size}")
    private String name;

    @NotBlank(message = "{agency.address.notblank}")
    private String address;

    @NotBlank(message = "{agency.phone.notblank}")
    @Size(min = 9, max = 20, message = "{agency.phone.size}")
    private String phone;

    @Valid
    @NotNull(message = "{agency.user.notnull}")
    private UserRequest user;
}
