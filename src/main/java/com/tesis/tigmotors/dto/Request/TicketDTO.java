package com.tesis.tigmotors.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketDTO {
    private String id;

    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    private String username;

    @NotBlank(message = "La descripción no puede estar vacía")
    private String descripcion;

    private String estado;

    @NotNull(message = "El campo aprobado no puede ser nulo")
    private boolean aprobado;
}