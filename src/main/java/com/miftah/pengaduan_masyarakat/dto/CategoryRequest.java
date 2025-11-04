package com.miftah.pengaduan_masyarakat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "{category.name.notblank}")
    @Size(min = 3, max = 100, message = "{category.name.size}")
    private String name;

    @Size(max = 4000, message = "{category.description.size.max}")
    private String description;
}
