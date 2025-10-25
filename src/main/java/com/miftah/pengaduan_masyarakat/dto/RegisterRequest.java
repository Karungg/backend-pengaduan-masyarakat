package com.miftah.pengaduan_masyarakat.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "{user.username.notblank}")
    @Size(min = 3, max = 50, message = "{user.username.size}")
    private String username;

    @NotBlank(message = "{user.email.notblank}")
    @Email(message = "{user.email.email}")
    private String email;

    @NotBlank(message = "{user.password.notblank}")
    @Size(min = 8, max = 100, message = "{user.password.size.range}")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "{user.password.pattern}")
    private String password;

    @NotBlank(message = "{user.role.notblank}")
    private String role;
}
