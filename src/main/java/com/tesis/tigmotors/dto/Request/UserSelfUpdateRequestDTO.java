package com.tesis.tigmotors.dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserSelfUpdateRequestDTO {

    @Size(max = 50, message = "El nombre de usuario no debe exceder los 50 caracteres.")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "El nombre de usuario solo puede contener letras, números, '.', '_' y '-'.")
    private String username;

    @Size(max = 100, message = "El nombre del negocio no debe exceder los 100 caracteres.")
    @Pattern(regexp = "^[A-Za-zÀ-ÿ]+(\\s[A-Za-zÀ-ÿ]+)*$", message = "El nombre del negocio solo puede contener letras y un solo espacio entre palabras.")
    private String businessName;

    @Email(message = "El formato del correo electrónico es inválido.")
    private String email;

    @Pattern(
            regexp = "^\\+593\\d{9}$",
            message = "El número de teléfono debe comenzar con +593 y contener exactamente 9 dígitos sin espacios ni caracteres adicionales."
    )
    private String phoneNumber;
}
