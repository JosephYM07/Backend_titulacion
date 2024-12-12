package com.tesis.tigmotors.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class SolicitudAdminRequestDTO {

    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    private String username;

    @NotBlank(message = "La descripción inicial no puede estar vacía")
    @Length(max = 500, message = "La descripción inicial no puede exceder los 500 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9 À-ſ]+$", message = "La descripción inicial contiene caracteres no permitidos")
    private String descripcionInicial;

    @NotBlank(message = "La prioridad no puede estar vacía")
    @Length(max = 20, message = "La prioridad debe ser ALTA, MEDIA o BAJA")
    @Pattern(regexp = "^[a-zA-Z0-9 À-ſ]+$", message = "La prioridad contiene caracteres no permitidos")
    private String prioridad;

    @NotNull(message = "La cotización no puede ser nula")
    @Positive(message = "La cotización debe ser un valor positivo")
    private Double cotizacion;

    @NotBlank(message = "La descripción del trabajo no puede estar vacía")
    @Length(max = 500, message = "La descripción del trabajo no puede exceder los 500 caracteres")
    private String descripcionTrabajo;
}