package com.tesis.tigmotors.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "El nombre de usuario no puede quedar en blanco")
    @Size(min = 5, max = 20, message = "El nombre de usuario debe tener entre 5 y 20 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "El nombre de usuario solo puede contener letras, números, '.', '_' y '-'")
    String username;

    @NotBlank(message = "La contraseña no puede estar en blanco")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "La contraseña debe contener al menos una letra mayúscula, una letra minúscula, un dígito y un carácter especial."
    )

    @Size(min = 8, max = 20, message = "La contraseña debe tener entre 8 y 20 caracteres.")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "El nombre no puede quedar en blanco")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres.")
    @Pattern(regexp = "^[A-Za-zÀ-ÿ]+(\\s[A-Za-zÀ-ÿ]+)*$", message = "El nombre solo puede contener letras y espacios simples entre palabras.")
    String business_name;


    @NotBlank(message = "El correo electrónico no puede estar en blanco")
    @Pattern(
            regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$",
            message = "Formato de correo electrónico no válido"
    )
    @NotBlank(message = "El correo no puede estar en blanco")
    String email;


    @Pattern(
            regexp = "^\\+593\\d{9}$",
            message = "El número de teléfono debe comenzar con +593 y estar seguido de exactamente 9 dígitos"
    )
    String phone_number;
}
