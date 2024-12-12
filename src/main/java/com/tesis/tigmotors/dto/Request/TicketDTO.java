package com.tesis.tigmotors.dto.Request;


import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class TicketDTO {
    private String id;

    private String username;
    private String solicitudId;

    private String estado;

    // Nuevos campos añadidos
    private String descripcionInicial; // Descripción inicial del problema
    private String descripcionTrabajo;
    private String fechaCreacion;
    private String horaCreacion;
}