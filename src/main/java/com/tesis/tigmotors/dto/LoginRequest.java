package com.tesis.tigmotors.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @NotBlank(message = "El nombre de usuario no puede quedar en blanco")
    @Size(min = 5, max = 20, message = "El nombre de usuario debe tener entre 5 y 20 caracteres")
    @Pattern(
            regexp = "^[A-Za-z0-9._-]+$",
            message = "El nombre de usuario solo puede contener letras, números, '.', '_' y '-'"
    )
    String username;

    @NotBlank(message = "La contraseña no puede estar en blanco")
    @Size(min = 8, max = 30, message = "La contraseña debe tener entre 8 y 30 caracteres.")
    String password;
}
