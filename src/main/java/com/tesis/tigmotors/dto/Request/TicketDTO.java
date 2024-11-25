package com.tesis.tigmotors.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketDTO {
    private String id;

    private String username;
    private String solicitudId;
    @NotBlank(message = "La descripción no puede estar vacía")
    private String descripcion;

    private String estado;

    @NotNull(message = "El campo aprobado no puede ser nulo")
    private boolean aprobado;

    // Nuevos campos añadidos
    private String descripcionInicial; // Descripción inicial del problema
    private String descripcionTrabajo;
}