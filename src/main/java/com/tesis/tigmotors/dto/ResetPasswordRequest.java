package com.tesis.tigmotors.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;


@Data
public class ResetPasswordRequest {
    @NotBlank(message = "El token no puede estar vacío")
    @Column(nullable = false)
    private String token;

    @NotBlank(message = "La nueva contraseña no puede estar vacía")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "La contraseña debe contener al menos una letra mayúscula, una letra minúscula, un dígito y un carácter especial."
    )
    @Size(min = 8, max = 20, message = "La contraseña debe tener entre 8 y 20 caracteres.")
    @Column(nullable = false)
    private String newPassword;

}
