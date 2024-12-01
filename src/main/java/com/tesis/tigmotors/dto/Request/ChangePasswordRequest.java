package com.tesis.tigmotors.dto.Request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para manejar los datos de la solicitud de cambio de contraseña.
 */
@Data
public class ChangePasswordRequest {

    @NotBlank(message = "La contraseña actual no puede estar vacía ni contener solo espacios.")
    @Size(min = 8, max = 20, message = "La contraseña actual debe tener entre 8 y 20 caracteres.")
    @Pattern(
            regexp = "^(?=.*\\S)(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&\\s]+$",
            message = "La contraseña actual debe contener al menos una letra mayúscula, una letra minúscula, un dígito y un carácter especial (@, $, !, %, *, ?, &)."
    )
    private String currentPassword;

    @NotBlank(message = "La nueva contraseña no puede estar vacía ni contener solo espacios.")
    @Size(min = 8, max = 20, message = "La nueva contraseña debe tener entre 8 y 20 caracteres.")
    @Pattern(
            regexp = "^(?=.*\\S)(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&\\s]+$",
            message = "La nueva contraseña debe contener al menos una letra mayúscula, una letra minúscula, un dígito y un carácter especial (@, $, !, %, *, ?, &)."
    )
    private String newPassword;
}
