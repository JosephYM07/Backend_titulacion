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

    private String estado;

    @NotBlank(message = "La prioridad no puede estar vacía")
    @Length(max = 20, message = "La prioridad debe ser ALTO - MEDIO - BAJO")
    @Pattern(regexp = "^[a-zA-Z0-9 À-ſ]+$", message = "La prioridad contiene caracteres no permitidos")
    private String prioridad;

    private Double cotizacion;
    private String cotizacionAceptada;

    private String fechaCreacion;
    private String horaCreacion;
}
