package com.tesis.tigmotors.dto.Request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserUpdateRequestDTO {

    @NotNull(message = "El ID del usuario es obligatorio")
    @Pattern(regexp = "^[1-100]*$", message = "El ID del usuario debe ser un número entero.")
    private Integer userId;

    @NotBlank(message = "El nombre de usuario no puede quedar en blanco.")
    @Size(min = 5, max = 20, message = "El nombre de usuario debe tener entre 5 y 20 caracteres.")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "El nombre de usuario solo puede contener letras, números, '.', '_' y '-'.")
    private String username;

    @NotBlank(message = "El nombre no puede quedar en blanco.")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres.")
    @Pattern(regexp = "^[A-Za-zÀ-ÿ]+(\\s[A-Za-zÀ-ÿ]+)*$", message = "El nombre solo puede contener letras y un solo espacio entre palabras, sin caracteres especiales ni números.")
    private String business_name;

    @NotBlank(message = "El correo electrónico no puede quedar en blanco.")
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "El correo electrónico debe ser válido (por ejemplo: usuario@dominio.com)."
    )
    private String email;

    @NotBlank(message = "El número de teléfono no puede quedar en blanco.")
    @Pattern(
            regexp = "^\\+593\\d{9}$",
            message = "El número de teléfono debe comenzar con +593 y contener exactamente 9 dígitos sin espacios ni caracteres adicionales."
    )
    private String phone_number;
}
