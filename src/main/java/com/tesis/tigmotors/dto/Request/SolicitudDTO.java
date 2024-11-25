package com.tesis.tigmotors.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class SolicitudDTO {

    private String idSolicitud;
    @NotBlank(message = "La descripción inicial no puede estar vacía")
    @Length(max = 500, message = "La descripción inicial no puede exceder los 500 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9 À-ſ]+$", message = "La descripción inicial contiene caracteres no permitidos")
    private String descripcionInicial;

    @Length(max = 500, message = "La descripción del trabajo no puede exceder los 500 caracteres")
    private String descripcionTrabajo;

    @NotBlank(message = "El estado no puede estar vacío")
    @Length(max = 50, message = "El estado no puede exceder los 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9 À-ſ]+$", message = "El estado contiene caracteres no permitidos")
    private String estado;

    @NotBlank(message = "La prioridad no puede estar vacía")
    @Length(max = 20, message = "La prioridad no puede exceder los 20 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9 À-ſ]+$", message = "La prioridad contiene caracteres no permitidos")
    private String prioridad;

    private String cotizacion; // Valor de la cotización (si aplica)
    private boolean cotizacionAceptada = false;
}
