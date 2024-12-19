package com.tesis.tigmotors.dto.Request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class SolicitudDTO {

    private String idSolicitud;
    private String username;
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


    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "El precio debe tener un máximo de 10 dígitos enteros y 2 decimales")
    private Double cotizacion;

    private String cotizacionAceptada;
    private String fechaCreacion;
    private String horaCreacion;
}
